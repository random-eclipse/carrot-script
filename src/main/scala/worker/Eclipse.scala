package worker

import scala.collection.JavaConverters._
import org.eclipse.swt.widgets.Display
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.ui.PlatformUI
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.ui.ISelectionService
import org.eclipse.ui.IWorkbench
import java.io.File
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import scala.io.Source
import org.eclipse.core.resources.IFile
import java.io.ByteArrayInputStream
import org.eclipse.core.runtime.Platform
import org.osgi.framework.Bundle
import org.slf4j.Logger

object Eclipse extends Logging {

  final val explorerId = "org.eclipse.jdt.ui.PackageExplorer";

  lazy val selectedResourceList : List[ IResource ] =
    if ( explorerSelection.isDefined ) explorerSelection.get
      .asInstanceOf[ IStructuredSelection ].toList.asScala
      .filter( _.isInstanceOf[ IAdaptable ] )
      .map( _.asInstanceOf[ IAdaptable ].getAdapter( classOf[ IResource ] ) )
      .filter( _ != null )
      .map( _.asInstanceOf[ IResource ] )
      .toList
    else
      List()

  lazy val selectedProjectList : List[ IProject ] = selectedResourceList.map( _.getProject ).distinct

  lazy val selectedProjectNameList : List[ String ] = selectedProjectList.map( _.getName )

  lazy val workspaceProjectList = workspace.getRoot.getProjects.toList

  def display : Display = Display.getDefault();

  def workspace : IWorkspace = ResourcesPlugin.getWorkspace

  def workspacePath : File = workspace.getRoot.getLocation.toFile

  def workbench : IWorkbench = PlatformUI.getWorkbench

  def selectionService : ISelectionService = workbench.getWorkbenchWindows()( 0 ).getSelectionService

  def selection( partId : String ) : Option[ ISelection ] = {
    var value : ISelection = null
    display.syncExec( new Runnable { def run { value = selectionService.getSelection( partId ) } } )
    if ( value == null ) None else Some( value )
  }

  def explorerSelection : Option[ ISelection ] = selection( explorerId )

  //  def project: IProject = projectList(0)

  //  def projectPath: File = project.getLocation.toFile

  def monitor = new NullProgressMonitor {
    var name = "invalid"
    override def beginTask( name : String, totalWork : Int ) {
      this.name = name
      logger.debug( s"Monitor ${name} init=${totalWork}" );
    }
    override def worked( work : Int ) {
      logger.debug( s"Monitor ${name} work=${work}" );
    }
    override def done() {
      logger.debug( s"Monitor ${name} done" )
    }
  }

  def monitorBlocker = new NullProgressMonitor {
    var name = "invalid"
    override def beginTask( name : String, totalWork : Int ) {
      this.name = name
      logger.info( s"Monitor ${name} init=${totalWork}" );
    }
    override def worked( work : Int ) {
      logger.info( s"Monitor ${name} work=${work}" );
    }
    override def done() {
      logger.info( s"Monitor ${name} done" )
      busy = false
    }
    @volatile
    var busy = true
    def await() {
      while ( busy ) {
        Thread.sleep( 100 )
      }
    }
  }

  //

  /** http://www.eclipse.org/articles/Article-Java-launch/launching-java.html  */

  /** Platform launcher manager. */
  def launchManager : ILaunchManager = DebugPlugin.getDefault.getLaunchManager

  /** Default default launch configuration name.*/
  def launchConfigName( typeName : String, instanceName : String ) = s"${typeName}[${instanceName}]"

  /** Discover configuration type provided by extension.*/
  def launchConfigType( typeName : String ) : ILaunchConfigurationType = launchManager.getLaunchConfigurationType( typeName )

  /** Create empty launch configuration with type/instance identity. */
  def launchConfigCreate( typeName : String, instanceName : String ) : ILaunchConfigurationWorkingCopy = {
    val config = launchConfigType( typeName ).newInstance( null, launchConfigName( typeName, instanceName ) )
    logger.debug( s"Created config=${config}" )
    config
  }

  /** Delete launch configuration with type/instance identity. */
  def launchConfigDelete( typeName : String, instanceName : String ) : Unit = {
    val configName = launchConfigName( typeName, instanceName )
    val configList = launchManager.getLaunchConfigurations( launchConfigType( typeName ) )
    for ( config <- configList if ( config.getName.equals( configName ) ) ) {
      config.delete
      logger.debug( s"Deleted config=${config}" )
    }
  }

  def textRead( file : IFile ) : String = Source.fromInputStream( file.getContents ).mkString

  def textWrite( file : IFile, string : String ) = file.setContents( new ByteArrayInputStream( string.getBytes ), true, false, null )

  def bundle( id : String ) : Bundle = Platform.getBundle( id )
  def bundleOption( id : String ) : Option[ Bundle ] = Option( Platform.getBundle( id ) )

  /** Space Master plugin bundle. */
  def masterBundle : Bundle = bundle( MASTER_ID )

  /** Space Master colsole logger. */
  def masterLogger : Logger = {
    val ctx = masterBundle.getBundleContext
    val ref = ctx.getServiceReference( classOf[ Logger ] )
    ctx.getService( ref )
  }

}
