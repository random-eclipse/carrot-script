package worker

import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.debug.internal.ui.views.console.ProcessConsole
import org.eclipse.debug.core.model.IProcess
import org.eclipse.debug.ui.console.ConsoleColorProvider
import org.eclipse.ui.console.IConsole
import scala.collection.JavaConverters._

object EclipseConsole extends A with Logging {

  private def console_time_stamp_key = "carrot_console_time_stamp"
  private def console_time_stamp_value = System.currentTimeMillis

  private final val consoleListLimit = 10

  private def manager = ConsolePlugin.getDefault.getConsoleManager

  private def timeStamp(console: ProcessConsole): Long = console.getAttribute(console_time_stamp_key).asInstanceOf[Long]

  private def disposeExpiredConsole = {
    val consoleList = manager.getConsoles.toList
      .filter(console => console.isInstanceOf[ProcessConsole])
      .map(console => console.asInstanceOf[ProcessConsole])
      .filter(console => console.getAttribute(console_time_stamp_key) != null && console.getProcess.isTerminated)
      .sortWith((consoleOne, consoleTwo) => timeStamp(consoleOne) < timeStamp(consoleTwo))
    val disposeList = consoleList
      .take(consoleList.size - consoleListLimit + 1)
    disposeList.foreach(console => manager.removeConsoles(Array[IConsole](console)))
  }

  /** Create new console for a process. */
  def console(process: IProcess): ProcessConsole = {
    disposeExpiredConsole
    val console = new ProcessConsole(process, new ConsoleColorProvider)
    console.setAttribute(console_time_stamp_key, console_time_stamp_value)
    manager.addConsoles(Array[IConsole](console))
    manager.showConsoleView(console)
    console
  }

  /** Bring to front named console. */
  def showConsole(name: String) = {
    val consoleList = manager.getConsoles.toList
    consoleList.foreach { console =>
      if (console.getName.equals(name)) manager.showConsoleView(console)
    }
  }

  /** Bring to front plugin console. */
  def showMasterConsole = showConsole(MASTER_NAME)

}
