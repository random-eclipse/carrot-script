

import worker.BaseAny
import worker.Eclipse._
import worker.ProxyManager._
import com.jcraft.jsch.JSch

object Proxy_Initialize extends BaseAny( "image/emblem-important.png" ) {
  override def execute = {
    logger.info( "proxy selector: " + selector )
    switchOn()
  }
}

object Proxy_Terminate extends BaseAny( "image/emblem-important.png" ) {
  override def execute = {
    logger.info( "proxy selector: " + selector )
    switchOff()
  }
}
