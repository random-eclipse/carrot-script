package worker

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkListener
import org.osgi.framework.FrameworkEvent
import org.osgi.framework.wiring.FrameworkWiring

import scala.collection.JavaConverters._

/**
 * worker logger which prints on master console
 */
trait Logging extends A {

  final lazy val logger = Eclipse.masterLogger

}

object Logging extends Logging {

  val SYSTEM = "system.bundle"
  val SLF4J_API = "org.slf4j.api"
  val SLF4J_IMPL_LOG4J = "org.slf4j.impl.log4j12"
  val SLF4J_IMPL_LOGBACK = "ch.qos.logback.slf4j"
  val M2E_LOGBACK_CONFIG = "org.eclipse.m2e.logback.configuration"
  val M2E_SLF4J_SIMPLE = "org.eclipse.m2e.maven.runtime.slf4j.simple"

  def reportBundle( name : String, entry : Option[ Bundle ] ) = entry match {
    case Some( bundle ) => logger.info( s"${name} : ${bundle.toString} : ${bundle.getState}" );
    case None           => logger.info( s"${name} : not present" )
  }

  /** Ensure slf4j -> logback binding for M2E */
  def configure() {

    val system = Eclipse.bundleOption( SYSTEM )
    val mavenConf = Eclipse.bundleOption( M2E_LOGBACK_CONFIG )
    val mavenSimple = Eclipse.bundleOption( M2E_SLF4J_SIMPLE )
    val slf4jApi = Eclipse.bundleOption( SLF4J_API )
    val slf4jImplLog4j = Eclipse.bundleOption( SLF4J_IMPL_LOG4J )
    val slf4jImplLogback = Eclipse.bundleOption( SLF4J_IMPL_LOGBACK )

    reportBundle( "system", system )
    reportBundle( "mavenConf", mavenConf )
    reportBundle( "mavenSimple", mavenSimple )
    reportBundle( "slf4jApi", slf4jApi )
    reportBundle( "slf4jImplLog4j", slf4jImplLog4j )
    reportBundle( "slf4jImplLogback", slf4jImplLogback )

    // restart mavenConf after refresh
    val onRefresh = new FrameworkListener {
      def frameworkEvent( event : FrameworkEvent ) {
        logger.info( s"onRefresh: event ${event} ${event.getType()}" )
        mavenConf.map { conf => conf.start() }
      }
    }

    def remove( bundleOption : Option[ Bundle ] ) = {
      for {
        sys <- system
        conf <- mavenConf
        bundle <- bundleOption
      } yield {
        logger.info( s"onRemove: ${bundle}" )
        conf.stop()
        bundle.uninstall()
        val list = List( conf ).asJava
        val wire = sys.adapt( classOf[ FrameworkWiring ] )
        wire.refreshBundles( list, onRefresh ) // purge "removal pending"
      }
    }

    // remove( mavenSimple )
    // remove( slf4jImplLog4j )

  }

}
