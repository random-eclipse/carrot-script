package worker

import scala.collection._
import scala.collection.JavaConverters._
import JGit._
import ModelCache._
import EclipseMaven._
import VersionPlugin._

case class ReleaseEntry(id: String, entry: CacheEntry)

object Release extends Logging {

  private val increment = (0, 0, 1, 0) // incremental version

  private lazy val releaseHistory = new java.util.TreeMap[String, ReleaseEntry]().asScala

  private var level = 0

  private def release(entry: CacheEntry) = {

    def model = entry.model

    logger.info(s"(${level}) Releasing ${model}")

    require(entry.canPerformRelease, s"Release aborted ${entry}")

    val identity = model.identity

    require(!releaseHistory.contains(identity), s"Release can not be duplicated. ${identity}")

    val versionCurrent =
      versionInstance(model.getVersion)

    val versionRelease =
      if (versionCurrent.isRelease) versionCurrent.increment(increment) else versionCurrent.release

    val versionSnapshot =
      versionRelease.increment(increment).snapshot

    logger.debug(s"Versions: current=${versionCurrent} release=${versionRelease} snapshot=${versionSnapshot}")

    val releaseTag = VersionTag(model.getArtifactId, versionRelease).toString
    logger.debug(s"Release tag=${releaseTag}")

    mavenEnsure(model,
      versionSet(model, versionCurrent, versionRelease,
        processProject = true, allowSnapshots = false).nonRecursive.build)

    require(versionInstance(model.getVersion).isRelease, "Release artifact has release version.")

    val id = model.getId

    val path = model.basedir

    gitAdd(path)(".")
    gitCommit(path)(s"[carrot-runner] Release ${model} ${versionRelease}")
    gitTagCreate(path)(releaseTag, forceUpdate = true)

    /** Maven Amazon. */
    //    mavenEnsure(model, "clean deploy -N -P attach-sources -D skipTests")

    /** Maven Central. */
    mavenEnsure(model, "clean deploy -N -P attach-sources -P attach-javadoc -P sign-artifacts -D skipTests -D updateReleaseInfo=true")

    mavenEnsure(model,
      versionSet(model, versionRelease, versionSnapshot,
        processProject = true, allowSnapshots = true).nonRecursive.build)

    require(versionInstance(model.getVersion).isSnapshot, "Develop artifact has snapshot version.")

    gitAdd(path)(".")
    gitCommit(path)(s"[carrot-runner] Develop ${model} ${versionSnapshot}")

    releaseHistory += identity -> ReleaseEntry(id, entry)

  }

  private def updateProjectParent(entry: CacheEntry) = {
    def model = entry.model
    logger.info(s"(${level}) Updating parent ${entry}")
    mavenEnsure(model, versionUpdateParentToRelease(model).nonRecursive.build)
  }

  private def updateProjectDependencies(entry: CacheEntry) = {
    def model = entry.model
    logger.info(s"(${level}) Updating dependencies ${model}")
    mavenEnsure(model, versionUpdateDependenciesToRelease(model).nonRecursive.build)
  }

  private def ensureParent(entry: CacheEntry) {
    def parent = entry.parent
    def parentUpdate = updateProjectParent(entry)
    def parentCascade = cascade(cacheEntryById(parent.getId).get)
    def parentRelease = release(cacheEntryById(parent.getId).get)
    if (entry.hasParent) {
      logger.info(s"(${level}) Ensure parent ${entry}")
      parentUpdate
      if (entry.hasSnapshotParent) { parentCascade; parentUpdate }
      if (entry.hasSnapshotParent) { parentRelease; parentUpdate }
      if (entry.hasSnapshotParent) { throw new IllegalStateException(s"Failed to release parent ${entry}") }
    }
  }

  private def ensureDependencies(entry: CacheEntry) {
    def dependencyUpdate = updateProjectDependencies(entry)
    def dependencyCascade = for (entry <- entry.dependencyEntryList()) { cascade(entry) }
    def dependencyRelease = for (entry <- entry.dependencyEntryList()) { release(entry) }
    if (entry.hasDependencies) {
      logger.info(s"(${level}) Ensure dependencies ${entry}")
      dependencyUpdate
      if (entry.hasSnapshotDependencies) { dependencyCascade; dependencyUpdate }
      if (entry.hasSnapshotDependencies) { dependencyRelease; dependencyUpdate }
      if (entry.hasSnapshotDependencies) { throw new IllegalStateException(s"Failed to release dependencies ${entry}") }
    }
  }

  private def ensureModules(entry: CacheEntry) {
    if (entry.isAggregator) {
      logger.info(s"(${level}) Ensure modules ${entry}")
      for (moduleEntry <- entry.moduleEntryList) cascade(moduleEntry)
    }
  }

  private def cascade(entry: CacheEntry): Unit = {
    level += 1
    logger.info(s"(${level}) Cascading ${entry}")
    if (entry.isLayoutProject) {
      ensureParent(entry)
      ensureDependencies(entry)
      ensureModules(entry)
    } else {
      entry.baseTimeReset
      ensureParent(entry)
      ensureDependencies(entry)
      if (entry.baseTimeChanged) release(entry)
      ensureModules(entry)
    }
    level -= 1
  }

  private def deploy(entry: CacheEntry): Unit = {
    def deployProject = mavenEnsure(entry.model, "clean deploy -N -D skipTests")
    def deployModuleList = for (moduleEntry <- entry.moduleEntryList) deploy(moduleEntry)
    logger.info(s"(${level}) Deploying ${entry}")
    if (entry.isLayoutProject) {
      deployModuleList
    } else if (entry.isAggregator) {
      deployProject
      deployModuleList
    } else {
      deployProject
    }
  }

  private def deployReleasedSnapshots =
    releaseHistory.values.foreach { tuple => deploy(tuple.entry) }

  private def releaseReport = {
    val report = releaseHistory
      .map { case (key, tuple) => s"   ${tuple.id}   ${tuple.entry.location}" }.toList.mkString("\n")
    if (report.isEmpty()) report else "\n" + report
  }

  def runnerRelease(entry: CacheEntry) = {
    release(entry)
    // deploy(entry)
    logger.info(s"(${level}) Commit repository ${entry}")
    gitPushTotal(entry.basedir)()
  }

  def runnerCascade(entry: CacheEntry): Unit = {
    val timeStart = System.currentTimeMillis()
    cascade(entry)
    logger.info(s"(${level}) Commit repository ${entry}")
    gitPushTotal(entry.basedir)()
    val size = releaseHistory.size
    logger.info(s"(${level}) Deploy updates: ${size}")
    deployReleasedSnapshots
    val timeFinish = System.currentTimeMillis()
    val unitTimeSec =
      if (size > 0) (timeFinish - timeStart) / size / 1000
      else 0
    logger.info(s"(${level}) Released projects: ${size} @ ${unitTimeSec} sec / project ${releaseReport}")
  }

}
