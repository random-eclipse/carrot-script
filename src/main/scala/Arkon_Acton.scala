import worker.BaseAny
import worker.Eclipse._
import com.jcraft.jsch.JSch
import worker.Logging

object Arkon extends BaseAny( "image/emblem-generic.png" ) {
  override def execute = {
    logger.info( "default action" )
    // Proxy_Initialize.execute
    // Logging.configure()
  }
}

object Arkon_Logging extends BaseAny( "image/emblem-generic.png" ) {
  override def execute = {
    logger.info( "Logging setup." )
    Logging.configure()
  }
}

object Arkon_UncaughtExceptionHandler extends BaseAny( "image/emblem-generic.png" ) {

  class Handler extends Thread.UncaughtExceptionHandler {
    def uncaughtException( thread : Thread, error : Throwable ) {
      logger.error( "   error: " + error.getMessage, error )
    }
  }

  override def execute = {
    logger.info( "Uncaught Exception Handler." )
    val handler = new Handler()
    Thread.setDefaultUncaughtExceptionHandler( handler );
  }

}
