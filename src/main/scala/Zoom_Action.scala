/** Enable by moving to default package. */
package zoom

import worker.Base
import worker.Eclipse._
import worker.EclipseMaven._
import worker.SoundClip

object Zoom_Audio extends Base {
  override def image = resourceStream( "image/emblem-important-3.png" )
  override def execute = {
    def play = SoundClip.success
    //    for { i <- 1 to 5 } {
    //      play
    //      Thread.sleep(4 * 1000)
    //    }
    play
  }
}

object Zoom_Selection extends Base {
  override def image = resourceStream( "image/emblem-important-3.png" )
  override def execute = {
    selectedResourceList.foreach { resource =>
      logger.info( s"resource=${resource}" )
    }
  }
}

object Zoom_Throwable extends Base {
  override def image = resourceStream( "image/emblem-important-3.png" )
  override def execute = {
    throw new Exception( "Produce Exception" )
  }
}

