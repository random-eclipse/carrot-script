package worker.tester

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.lang.invoke.MethodHandles
import worker.Logging

object MavenRegex extends Logging {

  val group_regex = "([^.]*[.][^.]*).*".r

  def organizationGroupId( groupId : String ) : String = {
    if ( groupId.contains( "." ) ) {
      val group_regex( groupPrefix ) = groupId
      groupPrefix
    } else {
      groupId
    }
  }

  def main( args : Array[ String ] ) = {

    logger.info( "init" )

    logger.info( s"group=${organizationGroupId( "simple" )}" )

    logger.info( s"group=${organizationGroupId( "com.carrotgarden" )}" )

    logger.info( s"group=${organizationGroupId( "com.carrotgarden.base.more" )}" )

    logger.info( s"group=${organizationGroupId( ".carrotgarden" )}" )

    logger.info( s"group=${organizationGroupId( "carrotgarden." )}" )

  }

}
