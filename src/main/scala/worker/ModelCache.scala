package worker

import java.io.File
import scala.collection.JavaConverters._
import scala.collection.JavaConverters._
import org.apache.maven.model.Dependency
import Eclipse._
import EclipseMaven._
import worker.model.MavenModel

/** Maven model which is updated on each access. */
case class CacheEntry(location: String) extends Logging {
  import CacheEntry._

  lazy val file = new File(location)

  private var time = 0L
  private var value: MavenModel = emptyModel(file)

  private def hasFile = file.exists && file.isFile
  private def hasChange = hasFile && file.lastModified != time
  private def updateTime = if (hasFile) time = file.lastModified
  private def updateModel = if (hasFile) value =
    try {
      updateTime
      mavenModelRead(file)
    } catch {
      case e: Throwable =>
        logger.warn(s"Failed to load ${file} ${e.getCause}")
        emptyModel(file)
    }
  private def update = if (hasChange) updateModel

  def model: MavenModel = {
    update
    value
  }

  private var base = 0L
  def baseTimeReset: Unit = base = time
  def baseTimeChanged: Boolean = base != time

  //

  def isArchon = model.isArchon
  def isAggregator = model.isAggregator
  def isLayoutProject = model.isLayoutProject
  def isEmpty = model.isEmpty

  def basedir = model.basedir
  def parent = model.getParent
  def hasParent = parent != null
  def hasDependencies = model.getDependencies().size > 0
  def hasSnapshotParent = hasParent && hasSnapshot(parent.getVersion)
  def hasSnapshotProject = hasSnapshot(model.getVersion)
  def hasDependencyManagement = model.getDependencyManagement != null

  def hasSnapshotDependencies = hasSnapshot(model.getDependencies) ||
    hasDependencyManagement && hasSnapshot(model.getDependencyManagement.getDependencies)

  def dependencyList = {
    def direct = model.getDependencies.asScala
    def managed = model.getDependencyManagement.getDependencies.asScala
    if (hasDependencyManagement) direct ++ managed
    else direct
  }

  def moduleEntryList =
    model.modulePomLocationList.map(locationEntry)

  def dependencyEntryList(dependencyFilter: Dependency => Boolean = selectSnapshot) =
    dependencyList.filter(dependencyFilter).map(dependencyEntry)

  def canPerformRelease: Boolean =
    if (isLayoutProject) { logger.info(s"Project is a layout manager: ${this}"); false }
    else if (hasSnapshotParent) { logger.info(s"Parent is a snapshot: ${parent}"); false }
    else if (hasSnapshotDependencies) { logger.info(s"Some dependency is a snapshot: ${this}"); false }
    else { true }

  def dependencyVersion(model: MavenModel): Option[String] =
    dependencyList
      .find { dependency => ModelCache.matchGroupArtifact(model, dependency) }
      .map { dependency => dependency.getVersion }

  override def toString = model.getId + " @ " + location

}

case object CacheEntry extends Logging {
  import ModelCache._

  final val snapshot = "SNAPSHOT"

  def locationEntry(location: String): CacheEntry =
    cacheEntryByLocation(location) match {
      case Some(entry) => entry
      case None => throw new IllegalStateException(s"Missing entry for location=${location}")
    }

  def dependencyEntry(dependency: Dependency): CacheEntry =
    cacheEntryByDependency(dependency) match {
      case Some(entry) => entry
      case None => throw new IllegalStateException(s"Missing entry for dependency=${dependency}")
    }

  def hasSnapshot(version: String): Boolean = version != null && version.contains(snapshot)

  def selectSnapshot(dependency: Dependency): Boolean = hasSnapshot(dependency.getVersion)

  def hasSnapshot(dependencyList: java.util.List[Dependency]): Boolean =
    dependencyList.asScala.exists(dependency => hasSnapshot(dependency.getVersion))

  def emptyModel(file: File) = MavenModel(file)
}

object ModelCache extends Logging {

  def cacheEntryById(id: String): Option[CacheEntry] =
    mavenModelCache.find(entry => entry.model.getId == id)

  def cacheEntryByLocation(location: String): Option[CacheEntry] =
    mavenModelCache.find(entry => entry.location == location)

  def cacheEntryByDependency(dependency: Dependency): Option[CacheEntry] =
    mavenModelCache.find { entry => matchGroupArtifact(entry, dependency) }

  def cacheEntrySetWithDependency(model: MavenModel): Set[CacheEntry] =
    mavenModelCache.filter { entry =>
      entry.dependencyList.exists { dependency => matchGroupArtifact(model, dependency) }
    }

  def matchGroupArtifact(model: MavenModel, dependency: Dependency): Boolean =
    model.getGroupId == dependency.getGroupId && model.getArtifactId == dependency.getArtifactId

  def matchGroupArtifact(entry: CacheEntry, dependency: Dependency): Boolean =
    matchGroupArtifact(entry.model, dependency)

  def buildCache = mavenModelCache

  private lazy val mavenModelCache: Set[CacheEntry] = {

    val set = collection.mutable.Set[CacheEntry]()

    def store(entry: CacheEntry) = {
      if (set.contains(entry)) {
      } else {
        set += entry
      }
    }

    def storeTree(entry: CacheEntry) {
      store(entry)
      for (location <- entry.model.modulePomLocationList) {
        storeTree(CacheEntry(location))
      }
    }

    logger.info(s"Workspace project count=${workspaceProjectList.size}")

    for {
      project <- workspaceProjectList
      basedir = project.getLocation.toFile
      pomFile = new File(basedir, "pom.xml")
      if (pomFile.exists && pomFile.isFile)
      location = pomFile.getAbsolutePath
    } {
      storeTree(CacheEntry(location))
    }

    logger.info(s"Maven pom project count=${set.size}")

    val emptyModelList = set.filter(entry => entry.isEmpty).toList
    if (emptyModelList.size > 0) {
      val invalidModelReport = emptyModelList.map(entry => s"   ${entry}").sorted.mkString("\n")
      logger.info(s"Invalid model report: ${emptyModelList.size} \n${invalidModelReport}")
    }

    set.toSet

  }

}
