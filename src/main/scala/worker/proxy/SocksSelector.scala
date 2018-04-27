package worker.proxy

import java.net.Proxy
import java.net.ProxySelector
import scala.sys.ShutdownHookThread
import scala.collection.concurrent.TrieMap
import java.net.URI
import java.net.SocketAddress
import java.io.IOException
import scala.collection.mutable.Queue

/**
 * Proxy Selector providing SSH based SOCKS server on demand.
 */
case class SocksSelector(time: Long = System.currentTimeMillis)
    extends ProxySelector with Base {

  val hook: ShutdownHookThread = sys.addShutdownHook(close)

  // addr:port -> socks-server
  val serverMap = new TrieMap[String, SocksServerSSH]()

  def clear() {
    val queue = serverMap.values.to[Queue]
    while (!queue.isEmpty) {
      queue.dequeue.close()
    }
    serverMap.clear()
  }

  def close() = {
    logger.info("close")
    clear()
    try { hook.remove() } catch { case e: Throwable => }
  }

  def select(uri: URI): java.util.List[Proxy] = {
    logger.info(s"select uri: ${uri}")
    val list = new java.util.ArrayList[Proxy]()
    val host = uri.getHost // first invocation brings unresolved host dns name
    val port = uri.getPort
    val id = uniqueID(uri) // addr:port
    val server = serverMap.getOrElseUpdate(id, SocksServerSSH(host, port))
    server.open()
    if (server.hasLocal) {
      logger.info(s"local success: ${server}")
      if (server.hasRemote) {
        logger.info(s"remote success: ${server}")
        list.add(server.proxy)
      } else {
        logger.error(s"remote faulure: ${server}")
        list.add(server.error)
      }
    } else {
      logger.error(s"local faulure: ${server}")
      server.close()
      list.add(server.error)
    }
    list
  }

  def connectFailed(uri: URI, address: SocketAddress, problem: IOException): Unit = {
    val id = uniqueID(uri)
    val option = serverMap.get(id)
    if (option.isDefined) {
      val server = option.get
      if (server.hasLocal) {
        // keep open
      } else {
        server.close()
      }
    }
    logger.error(s"remote: ${uri} local: ${address} problem: ${problem.getMessage}")
  }
}
