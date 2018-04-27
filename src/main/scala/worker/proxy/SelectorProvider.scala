package worker.proxy

import java.net.ProxySelector
import java.io.Closeable
import java.net.URI

import java.net.SocketAddress
import java.io.IOException

/**
 * Custom proxy selector provider with fall back to System selector.
 */
case class SelectorProvider(system: ProxySelector, custom: ProxySelector with Closeable)
    extends ProxySelector with Base {

  val proxyClasses = Base.proxyClasses
  
  def close = {
    custom.close()
  }

  def select(uri: URI): java.util.List[java.net.Proxy] = {
    if (isKnownURI(uri)) custom.select(uri) else system.select(uri)
  }

  def connectFailed(uri: URI, address: SocketAddress, problem: IOException): Unit = {
    if (isKnownURI(uri)) custom.connectFailed(uri, address, problem) else system.connectFailed(uri, address, problem)
  }

}
