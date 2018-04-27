package worker.proxy

import java.net.Proxy
import java.io.File
import scala.sys.process.Process
import scala.sys.process.ProcessLogger
import java.net.InetSocketAddress
import java.net.Socket

import worker.proxy.Base;

import java.net.InetAddress

/**
 * SSH based socks server provider.
 */
case class SocksServerSSH(
  /** target host */
  remoteHost: String, 
  /** target port */
  remotePort: Int, 
  /** target network interface used to attach target host public ip */
  remoteFace: String = "lo",
  /** socks server bind */
  localAddr: String = "127.0.0.1", 
  /** socks server port */
  localPort: Int = Base.localPort)
    extends Base {

  /** remote target address */
  val remoteAddr = InetAddress.getByName(remoteHost).getHostAddress

  /** socks server home folder */
  def workdir = new File(System.getProperty("java.io.tmpdir"))

  /** remote socks target */
  def remote = new InetSocketAddress(remoteAddr, remotePort)

  /** local socks server */
  def local = new InetSocketAddress(localAddr, localPort)

  /** error response address */
  def empty = new InetSocketAddress(localAddr, 0)

  /** normal working socks proxy */
  def proxy = new Proxy(Proxy.Type.SOCKS, local)

  /** error response socks proxy */
  def error = new Proxy(Proxy.Type.SOCKS, empty)

  /** configure remote target */
  def config_option = Seq("-n")
  def config_command = s"ip addr show dev ${remoteFace} | grep -q ${remoteAddr} || sudo ip addr add ${remoteAddr} dev ${remoteFace}"

  /** setup local socks server */
  def server_option = Seq("-n", "-N", "-D", s"${localAddr}:${localPort}")
  def server_command = "pwd"

  def ssh_result(options: Seq[String], command: String): Boolean = {
    val process = ssh_process(options, command)
    val result = process.exitValue()
    logger.info(s"result: ${result}")
    process.destroy()
    result == 0
  }

  def ssh_process(options: Seq[String], command: String): Process = {
    val builder = Process("ssh", options ++ Seq(remoteHost, command))
    logger.info(s"command: ${builder}")
    val log = ProcessLogger(String => None)
    val process = builder.run // (log)
    process
  }

  /** verify connection to socks server */
  def hasLocal = {
    try {
      val socket = new Socket(localAddr, localPort)
      socket.close()
      true
    } catch {
      case e: Throwable => false
    }
  }

  /** verify connection to remote socks target */
  def hasRemote = {
    try {
      val socket = new Socket(proxy)
      socket.connect(remote)
      socket.close()
      true
    } catch {
      case e: Throwable => false
    }
  }

  def hasChannel = hasLocal && hasRemote

  /** introduce channel setup delay */
  def ensure(limit: Int = 5, delay: Int = 500): Unit = {
    var count = limit
    while (count > 0) {
      count -= 1
      if (hasLocal) return
      Thread.sleep(delay)
    }
    logger.error(s"can not connect ${this}")
  }

  /** make remote target accept own public address */
  def config() = {
    if (!ssh_result(config_option, config_command)) {
      logger.error(s"can not config ${this}")
    }
  }

  /** start ssh socks server */
  def launch(): Process = {
    val process = ssh_process(server_option, server_command)
    ensure()
    process
  }

  @volatile
  private var process: Process = null

  /** initialize socks server */
  def open(): Unit = {
    logger.debug("open {}", this)
    this.synchronized {
      if (process == null) {
        config()
        process = launch()
      }
    }
  }

  /** terminate socks server */
  def close(): Unit = {
    logger.debug("close {}", this)
    this.synchronized {
      if (process != null) {
        process.destroy() // exit ssh process
        process = null
      }
    }
  }

  override def finalize() = try { close() } catch { case e: Throwable => }

}
