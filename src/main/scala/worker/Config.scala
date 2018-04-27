package worker

import Eclipse._
import java.io.File
import org.eclipse.core.resources.IResource
import scala.collection.JavaConverters._
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IPath
import Config._
import org.eclipse.core.resources.IFolder
import scala.util.Try
import Provide._
import JGit._
import worker.model.MavenVersion
import worker.model.MavenModel

case class VersionTag(identity: String, version: MavenVersion) extends Ordered[VersionTag] {
  import VersionTag._
  override def toString = apply(this)
  def increment(value: (Int, Int, Int, Int) = (0, 0, 1, 0)): VersionTag = VersionTag(identity, version.increment(value))
  def compare(that: VersionTag): Int = this.version.compareTo(that.version)
}

case object VersionTag {
  private val separator = "-"
  val version_pattern = """(\d+\.\d+\.\d+)"""
  val regex_tag = regex(".+")
  def apply(tag: String): VersionTag = {
    val data = regex_tag.findAllIn(tag).matchData.next
    val identity = data.group(1)
    val version = MavenVersion(data.group(2))
    VersionTag(identity, version)
  }
  def apply(tag: VersionTag) = s"${tag.identity}${separator}${tag.version}"
  def regex(identity: String) = (s"""(${identity})""" + separator + version_pattern).r
}

case class ConfigContext(master: IProject, version: IProject, identityPath: IPath) extends Logging {
  import ConfigContext._

  /** Instance folder on master branch. */
  def masterFolder = master.getFolder(identityPath)
  /** Instance folder on version branch. */
  def versionFolder = version.getFolder(identityPath)

  /** Instance file on master branch. */
  def masterFile = masterFolder.getFile(master_conf)
  /** Instance file on version branch. */
  def versionFile = versionFolder.getFile(version_conf)

  def masterText = textRead(masterFile)
  def versionText = textRead(versionFile)

  def masterText(string: String) = textWrite(masterFile, string)
  def versionText(string: String) = textWrite(versionFile, string)

  /** Discover maven projects inside master instance. */
  def masterModelList = mavenModelList(masterFolder)
  /** Discover maven projects inside version instance. */
  def versionModelList = mavenModelList(versionFolder)

  def findMaximumVersion(version: VersionTag): VersionTag = {
    val regex = VersionTag.regex(version.identity)
    val tagList = gitTagList(master.getLocation.toFile).asScala
    val nameList = tagList.map { _.getName.replaceAll(JGit.refsTags, "") }
    val properList = nameList.filter { regex.pattern.matcher(_).matches() }
    val versionList = properList.map { VersionTag(_) }
    if (versionList.isEmpty) version else List(version, versionList.max).max
  }

  /** Increment version tag in instance version file. */
  def incrementVersion: String = {
    gitPull(master.getLocation.toFile) // update to tip
    gitPull(version.getLocation.toFile) // update to tip
    val sourceConfig = versionText
    val sourceTagText = versionExtract(sourceConfig)
    val sourceTag = VersionTag(sourceTagText)
    val maximumTag = findMaximumVersion(sourceTag)
    logger.info("Maximum tag={}", maximumTag)
    val targetTag = maximumTag.increment()
    val targetTagText = targetTag.toString
    val targetConfig = versionReplace(sourceConfig, targetTagText)
    versionText(targetConfig)
    targetTagText
  }

  /** Transmit changes to remote repository. */
  def commitChanges(tag: String) = {
    def commitMaster {
      logger.info("Commit master changes.")
      val path = master.getLocation.toFile
      gitAdd(path)(".")
      gitCommit(path)(tag)
      gitTagCreate(path)(tag)
      gitPushTotal(path)()
    }
    def commitVersion {
      logger.info("Commit version changes.")
      val path = version.getLocation.toFile
      gitAdd(path)(".")
      gitCommit(path)(tag)
      // not tag
      gitPushTotal(path)()
    }
    EclipseConsole.showMasterConsole
    commitMaster
    commitVersion
  }

  def update: String = {
    logger.info(s"${this}")
    val tagName = incrementVersion
    commitChanges(tagName)
    logger.info(s"Release tag=${tagName}")
    tagName
  }

  /** @return trader.us.seed.01 */
  def identity = identityPath.toString
    .replaceAll(instance, "").replaceAll("/", ".")

  def versionTagRegex = VersionTag.regex(identity)

  override def toString = s"Context identity=${identity}"

}

case object ConfigContext extends Logging {

  private lazy val version_tag_regex =
    ("""^.*""" + carrot_config_version + """\s*=\s*([^#/\s]*).*$*""").r

  private def versionLine(text: String) = text.split("\n").find(_.contains(carrot_config_version)).get

  private def versionExtract(text: String): String = {
    val line = versionLine(text)
    val version_tag_regex(version) = line
    version
  }

  private def versionReplace(text: String, version: String): String = {
    val line = versionLine(text)
    text.replace(line, s"${carrot_config_version} = ${version} # ${stampDateTime}")
  }

}

object Config extends Logging {

  /** Configuration master branch marker file. */
  val config_master_marker = "config-master.md"

  /** Configuration version branch marker file. */
  val config_version_marker = "config-version.md"

  /** Configuration key property for version tracking. */
  val carrot_config_version = "carrot.config.version"

  /** Required file present in master branch instance folder. */
  val master_conf = "application.conf"

  /** Required file present in version branch instance folder. */
  val version_conf = "version.conf"

  /** Relative path to instance locations. */
  val instance = "instance/"

  /** Relative path to tags references in GIT database. */
  val refsTags = "refs/tags/"

  /** Enable instance-specific configuration actions. */
  def enable = !masterResourceList.isEmpty && !configContextList.isEmpty

  /** Instance folders in master branch. */
  lazy val masterResourceList: List[IResource] = selectedResourceList
    .filter(resource => new File(resource.getLocation.toFile, master_conf).exists)

  def identityPath(resource: IResource): IPath = resource.getProjectRelativePath

  /** Instance configurations in master and version branch. */
  lazy val configContextList: List[ConfigContext] =
    for {
      resource <- masterResourceList
      identityPath = resource.getProjectRelativePath
      master = resource.getProject
      option = workspaceProjectList.find { project =>
        project.getFolder(identityPath).getFile(version_conf).exists
      }
      if (option.isDefined)
      version = option.get
    } yield {
      ConfigContext(master, version, identityPath)
    }

  /** Instance maven projects in master and version branches. */
  lazy val configModelList: List[MavenModel] = configContextList
    .flatMap { context => context.masterModelList ++ context.versionModelList }

  /** Discover maven projects in an eclipse folder. */
  def mavenModelList(folder: IFolder): List[MavenModel] = folder.members.toList
    .filter(_.getName.endsWith(".xml"))
    .map(_.getLocation.toFile.getAbsoluteFile)
    .map(pomFile => Try(EclipseMaven.mavenModelRead(pomFile)))
    .filter(_.isSuccess)
    .map(_.get)
    .toList

  /** Remove all but last version tag matching a pattern. */
  def cleanupVersionTag(context: ConfigContext) = {

    logger.info(s"${context}")

    val list = List(context.master.getLocation.toFile, context.version.getLocation.toFile)

    list.foreach { path =>

      logger.info(s"${path}")

      JGit.gitPull(path)

      val regex = context.versionTagRegex

      val tagList = JGit.gitTagList(path).asScala

      val sourceTagList = for {
        tag <- tagList
        name = tag.getName.replaceAll(refsTags, "")
        find = regex.findFirstIn(name)
        if (find.isDefined)
      } yield {
        VersionTag(name)
      }

      val targetTagList = sourceTagList.sorted

      if (targetTagList.size > 1) {
        val deleteTagList = targetTagList.init.map { _.toString }
        JGit.gitTagDelete(path)(deleteTagList: _*)
        logger.info(s"Deleted tag count=${deleteTagList.size}")
      } else {
        logger.info(s"Nothing to delete.")
      }

    }

    list.foreach { path =>
      JGit.gitPull(path)
    }

  }

}
