package worker.model

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

/** Maven version bean extender. */
class MavenVersion private (version: String) extends DefaultArtifactVersion(version) {
  import MavenVersion._

  private def field(name: String) = {
    val field = classOf[DefaultArtifactVersion].getDeclaredField(name)
    field.setAccessible(true)
    field
  }

  private def fieldGet[T](name: String) = field(name).get(this).asInstanceOf[T]

  private def fieldSet[T](name: String, value: T): MavenVersion = {
    val that = MavenVersion(this)
    that.field(name).set(that, value)
    that
  }

  def majorVersion: Integer = fieldGet("majorVersion")
  def minorVersion: Integer = fieldGet("minorVersion")
  def incrementalVersion: Integer = fieldGet("incrementalVersion")
  def buildNumber: Integer = fieldGet("buildNumber")
  def qualifier: String = fieldGet("qualifier")

  def majorVersion(value: Int): MavenVersion = fieldSet("majorVersion", value)
  def minorVersion(value: Int): MavenVersion = fieldSet("minorVersion", value)
  def incrementalVersion(value: Int): MavenVersion = fieldSet("incrementalVersion", value)
  def buildNumber(value: Int): MavenVersion = fieldSet("buildNumber", value)
  def qualifier(value: String): MavenVersion = fieldSet("qualifier", value)

  def hasMajorVersion = majorVersion != null
  def hasMinorVersion = minorVersion != null
  def hasIncrementalVersion = incrementalVersion != null
  def hasBuildNumber = buildNumber != null
  def hasQualifier = qualifier != null

  override def toString = {
    val text = new StringBuilder
    if (hasMajorVersion) text.append("" + majorVersion)
    if (hasMinorVersion) text.append("." + minorVersion)
    if (hasIncrementalVersion) text.append("." + incrementalVersion)
    if (hasBuildNumber) text.append("." + buildNumber)
    if (hasQualifier) text.append("-" + qualifier)
    text.toString
  }

  def increment(value: (Int, Int, Int, Int)): MavenVersion = {
    val (major, minor, incremental, build) = value
    val that = MavenVersion(this)
    def increment(name: String, delta: Int) =
      that.field(name).set(that, that.field(name).get(that).asInstanceOf[Integer] + delta)
    if (that.hasMajorVersion) increment("majorVersion", major)
    if (that.hasMinorVersion) increment("minorVersion", minor)
    if (that.hasIncrementalVersion) increment("incrementalVersion", incremental)
    if (that.hasBuildNumber) increment("buildNumber", build)
    that
  }

  private final val SNAPSHOT = "SNAPSHOT"

  def release: MavenVersion = qualifier(null)
  def snapshot: MavenVersion = qualifier(SNAPSHOT)
  def isRelease = !hasQualifier || qualifier != SNAPSHOT
  def isSnapshot = hasQualifier && qualifier == SNAPSHOT

}

object MavenVersion {
  def apply(version: String) = new MavenVersion(version)
  def apply(version: MavenVersion) = new MavenVersion(version.toString)
}
