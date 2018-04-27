package worker.model

import org.apache.maven.model.Model
import scala.collection.JavaConverters._
import java.io.File
import worker.Logging
import worker.Provide

/** Maven model bean extender. */
class MavenModel private extends Model with Logging {
  import MavenModel._

  def basedir = getPomFile.getParentFile

  /** Project is a configuration parent. */
  def isArchon = (getPackaging == "pom")

  /** Project represents releasable project tree. */
  def isAggregator = isArchon && !getModules.isEmpty

  /** Project represents non-releasable project tree. */
  def isLayoutProject = isArchon && hasReleaseZero(getVersion)

  override def getGroupId = {
    if (super.getGroupId != null) super.getGroupId
    else getParent.getGroupId
  }

  override def getVersion = {
    if (super.getVersion != null) super.getVersion
    else getParent.getVersion
  }

  /** List of absolute paths of nested module pom.xml */
  def modulePomLocationList: List[String] = getModules.asScala
    .map { path => modulePomFile(basedir, path).getAbsolutePath }.toList

  def isEmpty = MavenModel.isEmpty(this)

  def identity = getGroupId() + ":" + getArtifactId()

}

object MavenModel {

  private def hasReleaseZero(version: String): Boolean = version != null && version == "0.0.0"

  private def modulePomFile(basedir: File, modulePath: String): File = {
    val entry = new File(basedir, modulePath)
    val pomFile = if (entry.isDirectory) new File(entry, "pom.xml") else entry
    pomFile.getAbsoluteFile
  }

  private lazy val fieldList = {
    val fieldList = Provide.fieldList(classOf[Model])
    fieldList.foreach(_.setAccessible(true))
    fieldList
  }

  def apply(model: Model): MavenModel = {
    val source = model.clone
    val target = new MavenModel
    fieldList.foreach(field => field.set(target, field.get(source)))
    target
  }

  private val empty_group = "empty-group"
  private val empty_artifact = "empty-artifact"
  private val empty_version = "0.0.0"

  private def isEmpty(model: MavenModel) =
    model.getGroupId == empty_group && model.getArtifactId == empty_artifact && model.getVersion == empty_version

  def apply(pomFile: File): MavenModel = {
    val model = new MavenModel
    model.setGroupId(empty_group)
    model.setArtifactId(empty_artifact)
    model.setVersion(empty_version)
    model.setPomFile(pomFile)
    model
  }

}
