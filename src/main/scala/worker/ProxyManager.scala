package worker

import java.net.ProxySelector
import java.net.URI
import java.net.SocketAddress
import java.io.IOException
import scala.collection.mutable.Queue
import java.net.ServerSocket
import java.io.File
import java.io.Closeable
import scala.sys.process.ProcessLogger
import scala.collection.mutable
import java.net.InetSocketAddress
import scala.collection.concurrent.TrieMap
import scala.sys.ShutdownHookThread
import worker.proxy.SelectorProvider
import worker.proxy.SocksSelector

/**
 * Proxy ON/OFF controller.
 */
object ProxyManager extends Logging {

  def selector = ProxySelector.getDefault

  def selector(next: ProxySelector) = ProxySelector.setDefault(next)

  val keyPast = "worker.proxy.system"
  val keyNext = "worker.proxy.custom"

  def system = System.getProperties.getOrDefault(keyPast, null).asInstanceOf[ProxySelector]
  def custom = System.getProperties.getOrDefault(keyNext, null).asInstanceOf[ProxySelector with Closeable]
  def system(selector: ProxySelector) = System.getProperties.put(keyPast, selector)
  def custom(selector: ProxySelector) = System.getProperties.put(keyNext, selector)

  def haveSystem = system != null
  def haveCustom = custom != null

  def switchOff() {

    if (haveSystem) {
      selector(system)
    }

    if (haveCustom) {
      custom.close()
    }

    logger.info("system: {}", system)
    logger.info("custom: {}", custom)
  }

  def switchOn() {

    if (!haveSystem) {
      system(selector)
    }

    if (haveCustom) {
      custom.close()
    }

    custom(SelectorProvider(system, SocksSelector()))

    logger.info("system: {}", system)
    logger.info("custom: {}", custom)

    selector(custom)

  }

}
