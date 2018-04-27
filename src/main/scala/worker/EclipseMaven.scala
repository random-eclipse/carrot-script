package worker

import java.io.File
import scala.collection.JavaConverters._
import org.apache.maven.project.MavenProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager._
import org.eclipse.debug.core.Launch
import org.eclipse.debug.core.model.IProcess._
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants._
import org.eclipse.m2e.actions.MavenLaunchConstants._
import org.eclipse.m2e.core.MavenPlugin
import org.eclipse.m2e.core.embedder.ICallable
import org.eclipse.m2e.core.embedder.IMaven
import org.eclipse.m2e.core.embedder.IMavenExecutionContext
import org.eclipse.m2e.internal.launch.MavenLaunchDelegate
import org.eclipse.m2e.internal.launch.MavenSourceLocator
import org.apache.maven.model.Dependency
import org.apache.maven.model.Parent
import org.eclipse.m2e.core.embedder.MavenModelManager
import org.eclipse.m2e.core.project.IMavenProjectRegistry
import org.eclipse.core.resources.IProject
import scala.util.Try
import Eclipse._
import Provide._
import ModelCache._
import VersionPlugin._
import worker.model.MavenModel
import org.eclipse.debug.internal.ui.DebugUIPlugin
import org.osgi.framework.Bundle
import org.osgi.framework.wiring.BundleWiring
import org.osgi.framework.wiring.FrameworkWiring
import org.osgi.framework.FrameworkListener
import org.osgi.framework.FrameworkEvent

/** M2E features */
object EclipseMaven extends Logging {

  /** Options for maven java invocation. */
  final val java_OPTS = "-Xms512m -Xmx512m"

  /** Options for all maven executions. */
  final val maven_OPTS = "  "

  def enable = !selectedMavenModelList.isEmpty

  lazy val selectedMavenProjectList : List[ IProject ] =
    selectedProjectList.filter( project => mavenPomFile( project.getLocation.toFile ).exists )

  lazy val selectedMavenModeProjectList : List[ MavenModel ] = selectedMavenProjectList
    .map( project => mavenModelRead( mavenPomFile( project.getLocation.toFile ) ) )

  lazy val selectedMavenModeResourceList : List[ MavenModel ] = selectedResourceList
    .filter( _.getName.endsWith( ".xml" ) )
    .map( resource => Try( mavenModelRead( resource.getLocation.toFile ) ) )
    .filter( _.isSuccess )
    .map( _.get )

  lazy val selectedMavenModelList : List[ MavenModel ] =
    if ( selectedMavenModeResourceList.isEmpty ) selectedMavenModeProjectList
    else selectedMavenModeResourceList

  def selectedMavenModelEach( function : MavenModel => Unit ) = selectedMavenModelList.foreach( model => function( model ) )

  private def maven : IMaven = MavenPlugin.getMaven

  private def mavenContext : IMavenExecutionContext = maven.createExecutionContext

  private def modelManager : MavenModelManager = MavenPlugin.getMavenModelManager

  private def projectRegistry : IMavenProjectRegistry = MavenPlugin.getMavenProjectRegistry

  private def mavenPomFile( basedir : File ) : File = new File( basedir, "pom.xml" )

  def mavenEntry( pomFile : File ) : CacheEntry = CacheEntry( pomFile.getAbsolutePath ) // XXX

  def mavenProjectRead( pomFile : File ) : MavenProject = mavenContext.execute( new ICallable[ MavenProject ] {
    override def call( context : IMavenExecutionContext, monitor : IProgressMonitor ) = {
      val project = maven.readProject( pomFile, monitor )
      project
    }
  }, monitor )

  def mavenModelRead( pomFile : File ) : MavenModel = mavenContext.execute( new ICallable[ MavenModel ] {
    override def call( context : IMavenExecutionContext, monitor : IProgressMonitor ) = {
      val model = maven.readModel( pomFile )
      model.setPomFile( pomFile )
      MavenModel( model )
    }
  }, monitor )

  def resourceId( parent : Parent ) : String = parent.getId

  def resourceId( model : MavenModel ) : String = model.getId

  def resourceId( dependency : Dependency ) : String = {
    val id = new StringBuilder
    id.append( dependency.getGroupId )
    id.append( ":" );
    id.append( dependency.getArtifactId )
    id.append( ":" );
    id.append( dependency.getType ) // jar
    id.append( ":" );
    id.append( if ( dependency.getVersion == null ) "[inherited]" else dependency.getVersion )
    id.toString
  }

  private def mavenConfigCreate( instanceName : String ) : ILaunchConfigurationWorkingCopy =
    Eclipse.launchConfigCreate( LAUNCH_CONFIGURATION_TYPE_ID, instanceName )

  private def mavenConfigDelete( instanceName : String ) =
    Eclipse.launchConfigDelete( LAUNCH_CONFIGURATION_TYPE_ID, instanceName )

  def mavenResult( model : MavenModel, command : String ) : Boolean = {
    mavenLaunch( model, command ).getProcesses()( 0 ).getExitValue == 0
  }

  def mavenEnsure( model : MavenModel, command : String ) : Unit = {
    logger.info( s"Project: ${model.getId} @ ${model.getPomFile}" )
    logger.info( s"Command: ${command}" )
    if ( mavenResult( model, command ) )
      logger.info( s"Success." )
    else
      throw new Exception( s"Failure. Review process console." )
  }

  def mavenLaunch( model : MavenModel, command : String,
                   enableConsole : Boolean = true, enableBlocking : Boolean = true ) : ILaunch = {
    val attributeMap = collection.immutable.Map.newBuilder[ String, Any ]
    attributeMap += ATTR_POM_DIR -> model.basedir.getAbsolutePath
    attributeMap += ATTR_GOALS -> ( command + " -f " + model.getPomFile.getAbsolutePath + maven_OPTS )
    attributeMap += ATTR_VM_ARGUMENTS -> java_OPTS
    mavenLaunch( model, attributeMap.result, enableConsole, enableBlocking )
  }

  def mavenLaunch( model : MavenModel, attributeMap : Map[ String, Any ],
                   enableConsole : Boolean, enableBlocking : Boolean ) : ILaunch = {
    //    logger.info(s"attribute map\n${attributeMap.mkString("\n")}")
    val instanceName = model.getArtifactId
    try {

      val config = mavenConfigCreate( instanceName )
      config.setAttributes( attributeMap.asJava )

      val locator = new MavenSourceLocator
      locator.initializeDefaults( config )

      val launch = new Launch( config, RUN_MODE, locator )

      val mode = RUN_MODE

      val monitor = Eclipse.monitor

      val manager = new MavenLaunchDelegate
      manager.launch( config, mode, launch, monitor )

      val process = launch.getProcesses()( 0 )
      val label = s"${stampTime} ${instanceName} [mvn ${attributeMap( ATTR_GOALS )} ]"
      process.setAttribute( ATTR_PROCESS_LABEL, label )
      val command = process.getAttribute( ATTR_CMDLINE )
      //      logger.info(s"Launch command=${command}")

      if ( enableConsole ) {
        //        logger.info("Enable console.")
        EclipseConsole.console( process )
      }
      if ( enableBlocking ) {
        //        logger.info("Enable blocking.")
        while ( !launch.isTerminated ) {
          //          logger.info("Await termination.")
          Thread.sleep( 100 )
        }
      }
      //      Thread.sleep(3 * 1000)
      launch
    } finally {
      mavenConfigDelete( instanceName )
    }
  }

  def commandBuilder = new CommandBuilder

}

class CommandBuilder {
  private val text = new StringBuilder
  def entry( value : String ) = { text.append( s" ${value} " ); this }
  def option( key : String, value : Any ) = { text.append( s" -D ${key}=${value} " ); this }
  def nonRecursive = entry( "-N" )
  def build = text.toString
}
