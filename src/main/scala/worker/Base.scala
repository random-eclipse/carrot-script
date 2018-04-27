package worker

import java.io.InputStream
import Eclipse.selectedProjectNameList
import java.io.File

/** Public interface exposed by worker to master. */
trait Base extends A with Logging {
  import java.io.InputStream
  import Eclipse.selectedProjectNameList

  /** Enable command in plugin menu. */
  def enable : Boolean =
    try { check }
    catch { case e : Throwable => logger.error( "Enable failure", e ); false }

  /** Decorate command with an icon in plugin menu. */
  def image : InputStream = resourceStream( "image/emblem-generic.png" )

  /** Execute command when selected in plugin menu. */
  def invoke : Any = {
    val summary = selectedProjectNameList
    try {
      val timeStart = System.currentTimeMillis
      showConsole
      logger.info( s"Start: ${summary}" )
      execute
      val timeFinish = System.currentTimeMillis
      val timeDuration = ( timeFinish - timeStart ) / 1000
      logger.info( s"Finish: ${summary} @ ${timeDuration} sec." )
      // SoundClip.success
      "Success."
    } catch {
      case e : Throwable =>
        logger.error( s"Execution failure: ${summary}", e )
        // SoundClip.failure
        "Failure."
    } finally {
      logger.info( "\n" )
      showConsole
    }
  }

  /** Wrapped enable check. */
  protected def check : Boolean = true

  /** Wrapped command execution. */
  protected def execute : Unit = logger.info( "Command execution." )

  /** Read icon image from class path. */
  protected def resourceStream( path : String ) : InputStream = getClass.getResourceAsStream( path )

  /** Bring to front plugin console. */
  protected def showConsole = EclipseConsole.showMasterConsole

}

abstract class BaseAny( icon : String ) extends Base {
  def resource = worker.resource.A.path
  override def image = resourceStream( s"${resource}/${icon}" )
}

abstract class BaseMaven( icon : String ) extends BaseAny( icon ) {
  def optsTrace = " -e "
  def optsNoTest = " -D skipTests -D invoker.skip=true " 
  override def check = EclipseMaven.enable
}

abstract class BaseConfig( icon : String ) extends BaseAny( icon ) {
  override def check = Config.enable
}

abstract class BaseRepo( icon : String ) extends BaseAny( icon ) {
  override def check = JGit.enable
}

abstract class BaseSBT( icon : String ) extends BaseAny( icon ) {
  import scala.sys.process.Process
  import scala.sys.process.ProcessLogger
  import worker.Eclipse._
  import org.eclipse.core.resources._

  val buildFile = "build.sbt"

  def isScalaBuild( resource : IResource ) : Boolean = {
    if ( resource.isInstanceOf[ IFile ] ) {
      //val file = resource.asInstanceOf[IFile]
      //file.exists() && file.getName.endsWith(".sbt")
      false
    } else if ( resource.isInstanceOf[ IContainer ] ) {
      val container = resource.asInstanceOf[ IContainer ]
      container.findMember( buildFile ) != null
    } else {
      false
    }
  }

  def sbt_log( message : String ) : Unit = {
    logger.info( message )
  }

  def sbt_process( workdir : File, command : String ) : Process = {
    val builder = Process( Seq( "sbt", "-Dsbt.log.noformat=true", command ), workdir )
    logger.info( s"command: ${builder}" )
    val log = ProcessLogger( sbt_log _ )
    val process = builder.run( log )
    process
  }

  def sbt_result( workdir : File, command : String ) : Boolean = {
    val process = sbt_process( workdir, command )
    val result = process.exitValue()
    logger.info( s"result: ${result}" )
    process.destroy()
    result == 0
  }

  lazy val scalaProjectList = selectedResourceList.filter { isScalaBuild( _ ) }

  override def check = !scalaProjectList.isEmpty

  override def execute = {
    scalaProjectList.map { path =>
      val workdir = path.getLocation.toFile()
      logger.info( s"workdir: ${workdir}" )
      sbt_result( workdir, command )
    }
  }

  def command : String

}
