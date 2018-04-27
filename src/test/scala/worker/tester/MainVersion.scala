package worker.tester

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.lang.invoke.MethodHandles
import worker.Logging
import worker.model.MavenVersion
import worker.VersionTag

class MavenVersionTest( version : String ) extends DefaultArtifactVersion( version ) {

  final val lookup = MethodHandles.lookup

  def majorVersionXXX( value : Int ) = lookup.findSetter( getClass, "majorVersion", classOf[ Integer ] )
    .invoke( this, new Integer( value ) )

  def field( name : String ) = {
    val field = classOf[ DefaultArtifactVersion ].getDeclaredField( name )
    field.setAccessible( true )
    field
  }

  def majorVersion : Integer = field( "majorVersion" ).get( this ).asInstanceOf[ Integer ]
  def minorVersion = field( "minorVersion" ).get( this ).asInstanceOf[ Integer ]
  def incrementalVersion = field( "incrementalVersion" ).get( this ).asInstanceOf[ Integer ]
  def buildNumber = field( "buildNumber" ).get( this ).asInstanceOf[ Integer ]
  def qualifier = field( "qualifier" ).get( this ).asInstanceOf[ String ]

  def majorVersion( value : Integer ) = field( "majorVersion" ).set( this, value )
  def minorVersion( value : Integer ) = field( "minorVersion" ).set( this, value )
  def incrementalVersion( value : Integer ) = field( "incrementalVersion" ).set( this, value )
  def buildNumber( value : Integer ) = field( "buildNumber" ).set( this, value )
  def qualifier( value : String ) = field( "qualifier" ).set( this, value )

  def hasMajorVersion = majorVersion != null
  def hasMinorVersion = minorVersion != null
  def hasIncrementalVersion = incrementalVersion != null
  def hasBuildNumber = buildNumber != null
  def hasQualifier = qualifier != null

}

object MavenVersionTest extends Logging {

  def main( args : Array[ String ] ) = {

    logger.info( "init" )

    val version = MavenVersion( "1.2.3-SNAPSHOT" )
    logger.info( s"version original=${version}" )

    logger.info( s"version increment=${version.increment( 1, 2, 0, 0 )}" )

    logger.info( s"version release=${version.release}" )

    logger.info( s"version snapshot=${version.snapshot}" )

    //

    val regex = VersionTag.regex( "testing" )
    logger.info( s"regex=${regex}" )

    //

    val versionTag = """(.+)-(\d+\.\d+\.\d+)""".r

    logger.info( s"${versionTag.findAllIn( "hello-1.2.3" ).matchData.next.group( 2 )}" )

    logger.info( s"${VersionTag( "trader.us.seed.01-1.2.3" ).identity}" )
    logger.info( s"${VersionTag( "trader.us.seed.01-1.2.3" ).version}" )

  }

}
