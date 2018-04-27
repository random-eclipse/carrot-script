package worker.proxy

import java.net.ServerSocket
import java.net.URI
import java.io.Closeable
import java.io.File
import scala.collection.concurrent.TrieMap
import java.net.InetAddress
import worker.Logging

trait Base extends Logging with Closeable {

  logger.info("init")

  def uniqueID(uri: URI) = Base.uniqueID(uri)

  def isKnownURI(uri: URI) = Base.isKnownURI(uri)

}

object Base extends Logging {

  val RX_HOST = """.+.carrotgarden.com|.+[.]lan""".r //
  val RX_PORT = """9\d\d\d"""".r //

  val knownMap = new TrieMap[String, String]()

  def uniqueID(uri: URI) = {
    val host = uri.getHost
    val port = uri.getPort
    val addr = InetAddress.getByName(host).getHostAddress
    s"${addr}:${port}"
  }

  // match hosts
  def isKnownURI(uri: URI) = {
    val host = uri.getHost
    val port = Integer.toString(uri.getPort)
    val id = uniqueID(uri)

    if (knownMap.contains(id)) {
      logger.debug(s"known: ${id}")
      true
    } else {
      val hasHost = RX_HOST.pattern.matcher(host).matches()
      val hasPort = RX_PORT.pattern.matcher(port).matches()
      logger.debug(s"match: ${host} ${hasHost} ${port} ${hasPort}")
      val hasMatch = hasHost // && hasPort
      if (hasMatch) knownMap put (id, id)
      hasMatch
    }

  }

  // find free port 
  def localPort = {
    val socket = new ServerSocket(0);
    val port = socket.getLocalPort
    socket.close()
    port
  }

  // load and keep lib.proxy classes 
  def proxyClasses() = {
    val klazMap = new TrieMap[String, Class[_]]()
    val rootUrl = getClass.getProtectionDomain.getCodeSource.getLocation
    val baseUrl = getClass.getResource(".")
    val protocol = baseUrl.getProtocol
    require("file".equals(protocol))
    val root = rootUrl.getPath
    val base = baseUrl.getPath
    val list = new File(base).listFiles
    list
      .filter { file =>
        file.getName.endsWith(".class")
      }
      .foreach { file =>
        val name = file.getAbsolutePath
          .replace(root, "")
          .replace(".class", "")
          .replaceAll("/", ".")
        val klaz = Class.forName(name)
        klazMap.put(name, klaz)
      }
    klazMap
  }

}
