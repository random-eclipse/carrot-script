package worker

import EclipseMaven._
import worker.model.MavenVersion
import worker.model.MavenModel

object VersionPlugin {

  //  def versionTag(entry: CacheEntry, version: MavenVersion) =
  //    s"${entry.model.getArtifactId}-${version}"

  def versionInstance(version: String): MavenVersion = MavenVersion(version)

  //

  private final val plugin = "org.codehaus.mojo:versions-maven-plugin"

  private lazy val group_regex = "([^.]*[.][^.]*).*".r

  /** com.carrotgarden.wrap */
  private lazy val exclude_pattern = "*.wrap:*"

  /** com.carrotgarden */
  private def organizationGroupId(model: MavenModel): String = {
    val groupId = model.getGroupId
    if (groupId.contains(".")) {
      val group_regex(groupPrefix) = groupId
      groupPrefix
    } else {
      groupId
    }
  }

  def versionSet(model: MavenModel, versionOld: MavenVersion, versionNew: MavenVersion,
    processParent: Boolean = false,
    processProject: Boolean = false,
    processPlugins: Boolean = false,
    processDependencies: Boolean = false,
    updateMatchingVersions: Boolean = false,
    allowSnapshots: Boolean = false) =
    commandBuilder
      .entry(s"${plugin}::set")
      .option("groupId", model.getGroupId)
      .option("artifactId", model.getArtifactId)
      .option("oldVersion", versionOld)
      .option("newVersion", versionNew)
      .option("processParent", processParent)
      .option("processProject", processProject)
      .option("processPlugins", processPlugins)
      .option("processDependencies", processDependencies)
      .option("updateMatchingVersions", updateMatchingVersions)
      .option("allowSnapshots", allowSnapshots)
      .option("generateBackupPoms", false)

  def versionProjectSetXXX(model: MavenModel, versionOld: MavenVersion, versionNew: MavenVersion) =
    commandBuilder
      .entry(s"${plugin}::set")
      .option("groupId", model.getGroupId)
      .option("artifactId", model.getArtifactId)
      .option("oldVersion", versionOld)
      .option("newVersion", versionNew)
      .option("processParent", false)
      .option("processProject", true)
      .option("processPlugins", false)
      .option("processDependencies", false)
      .option("updateMatchingVersions", false)
      .option("generateBackupPoms", false)
      .option("allowSnapshots", true) // XXX

  /** Sets the parent version to the latest parent RELEASE version. */
  def versionUpdateParentToRelease(model: MavenModel) =
    commandBuilder
      .entry(s"${plugin}::update-parent")
      .option("parentVersion", s"[${versionInstance(model.getParent.getVersion).release},)")
      .option("generateBackupPoms", false)
      .option("allowSnapshots", false) // XXX

  /** Sets the parent version to the latest parent SNAPSHOT version. */
  def versionUpdateParentToSnapshot(model: MavenModel) =
    commandBuilder
      .entry(s"${plugin}::update-parent")
      .option("parentVersion", s"[${versionInstance(model.getParent.getVersion).release},)")
      .option("generateBackupPoms", false)
      .option("allowSnapshots", true) // XXX

  def versionUpdateDependenciesToRelease(model: MavenModel) =
    commandBuilder
      .entry(s"${plugin}::use-latest-versions")
      .option("includes", s"${organizationGroupId(model)}*:*")
      .option("excludes", s"${exclude_pattern}")
      .option("excludeReactor", false)
      .option("allowMajorUpdates", true)
      .option("allowMinorUpdates", true)
      .option("allowIncrementalUpdates", true)
      .option("generateBackupPoms", false)
      .option("allowSnapshots", false) // XXX

  def versionUpdateDependenciesToSnapshot(model: MavenModel) =
    commandBuilder
      .entry(s"${plugin}::use-latest-versions")
      .option("includes", s"${organizationGroupId(model)}*:*")
      .option("excludes", s"${exclude_pattern}")
      .option("excludeReactor", false)
      .option("allowMajorUpdates", true)
      .option("allowMinorUpdates", true)
      .option("allowIncrementalUpdates", true)
      .option("generateBackupPoms", false)
      .option("allowSnapshots", true) // XXX

  def versionReportUpdates(model: MavenModel) =
    commandBuilder
      .entry(s"${plugin}::display-plugin-updates")
      .entry(s"${plugin}::display-property-updates")
      .entry(s"${plugin}::display-dependency-updates")
      .option("processDependencies", true)
      .option("processDependencyManagement", true)
      .option("verbose", false)
      .option("generateBackupPoms", false)
      .option("allowSnapshots", false)
//      .entry("-X")

}
