package worker

import java.net.URLClassLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.lang.reflect.Field

object Provide extends Logging {

  def fieldList(klaz: Class[_]): List[Field] = {
    if (klaz == null) Nil
    else klaz.getDeclaredFields.toList ++ fieldList(klaz.getSuperclass)
  }

  def report(loader: ClassLoader, text: StringBuilder) {
    if (loader.isInstanceOf[URLClassLoader]) {
      text.append(loader);
      text.append("\n");
      val urls = loader.asInstanceOf[URLClassLoader].getURLs
      for (url <- urls) {
        text.append("\t")
        text.append(url)
        text.append("\n")
      }
      if (loader.getParent() != null) {
        report(loader.getParent, text)
      }
    } else {
      text.append("???=")
      text.append(loader)
      text.append("\n")
    }
  }

  def report(loader: ClassLoader): String = {
    val text = new StringBuilder
    report(loader, text)
    text.toString
  }

  lazy val formatTime = new SimpleDateFormat("hh:mm:ss")

  def stampTime = formatTime.format(new Date)

  lazy val formatDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z")

  def stampDateTime = formatDateTime.format(new Date)

  def time[R](fun: => R): R = {
    val t1 = System.nanoTime
    val ret = fun
    val t2 = System.nanoTime
    val diff = ((t2 - t1) / 1e6).asInstanceOf[Long]
    val name = Thread.currentThread.getStackTrace()(3).getMethodName
    //    logger.info(s"### ${Thread.currentThread.getStackTrace().mkString("\n")}")
    logger.info(s"### Time: ${diff} ms; Name: ${name}")
    ret
  }

}
