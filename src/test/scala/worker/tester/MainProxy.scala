package worker.tester

import java.net.ProxySelector
import java.net.URI
import java.net.SocketAddress
import java.io.IOException
import java.net.URL
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import java.net.Socket
import java.net.InetAddress
import java.net.NetworkInterface
import scala.collection.JavaConverters
import java.net.ServerSocket
import java.net.InetSocketAddress

import worker.Logging
import worker.ProxyManager
import worker.proxy.SocksServerSSH
import worker.proxy.SocksSelector

object MainProxy extends Logging {
  import ProxyManager._
  import JavaConverters._

  //
  //val addr = "serv-1.carrotgarden.com:9000"

  def main_x( args : Array[ String ] ) : Unit = {

    val face = NetworkInterface.getByName( "lo" )

    logger.info( s"face: ${face}" )

    val list = face.getInterfaceAddresses.asScala

    for {
      addr <- list
    } yield {
      logger.info( s"addr: ${addr.getAddress}" )
    }

    val one = list.head

    val inet = new InetSocketAddress( one.getAddress, 0 )

    val sock = new ServerSocket()

    sock.bind( inet )

  }

  def main( args : Array[ String ] ) : Unit = {

    val host = "serv-1.carrotgarden.com"
    val port = 9000
    val addr = s"${host}:${port}"

    // val socks = SocksServerSSH(host)

    //    System.setProperty("sun.rmi.transport.logLevel", "VERBOSE")
    //    System.setProperty("sun.rmi.transport.proxy.logLevel", "VERBOSE")

    //    System.setProperty("socksProxyHost", "localhost")
    //    System.setProperty("socksProxyPort", "1234")
    //    System.setProperty("socksNonProxyHosts", "")

    switchOn()

    // Base.ensureClasses()

    //val proxyAddr = custom.select(new URI(s"socket://${addr}")).get(0).address().asInstanceOf[InetSocketAddress]
    // Thread.sleep(3 * 1000)
    //    val proxyServ = new Socket(proxyAddr.getHostName, proxyAddr.getPort)
    //    logger.info(s"proxyServ ${proxyServ}")
    //    proxyServ.close()

    //

    //    try {
    //      val socket = new Socket(host, port)
    //      logger.info(s"socket ${socket}")
    //    } catch {
    //      case e: Throwable =>
    //        logger.error("", e)
    //    }

    //    val address = new InetSocketAddress("localhost", 1234)
    //    val proxy = new java.net.Proxy(java.net.Proxy.Type.SOCKS, address)
    //    val socket = new Socket(proxy)
    //    logger.info(s"socket ${socket}")
    //    socket.connect(new InetSocketAddress("localhost", 9000))

    // sys.exit()

    def test = try {
      val serviceURL = new JMXServiceURL( s"service:jmx:rmi:///jndi/rmi://${addr}/jmxrmi" )
      val environment = new java.util.HashMap[ String, Object ]()
      val connector = JMXConnectorFactory.newJMXConnector( serviceURL, environment )
      logger.info( s"connector: ${connector}" )
      connector.connect()
      connector.close()
    } catch {
      case e : Throwable =>
        logger.error( "", e )
    }

    test
    test
    test

    sys.exit()

  }

  def main_1( args : Array[ String ] ) : Unit = {

    logger.info( "init" )

    val host = "serv-1.dom"
    val port = 9000

    val socks = SocksServerSSH( host, port )

    logger.info( s"socks ${socks}" )

    socks.open()

    Thread.sleep( 1000 )

    socks.close()

    val selector = SocksSelector()

    val uri = new URI( s"http://${host}:25" )

    val list = selector.select( uri )

    logger.info( s"list ${list}" )

    val proxy = list.get( 0 )

    val conn = uri.toURL().openConnection( proxy )

    logger.info( s"conn ${conn}" )

    try {
      conn.connect()
    } catch {
      case e : Throwable =>
        logger.error( "", e )
    }

    //sys.exit()

  }

}
