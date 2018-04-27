package worker.tester

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import javax.sound.sampled.LineEvent.Type.CLOSE
import javax.sound.sampled.LineEvent.Type.OPEN
import javax.sound.sampled.LineEvent.Type.START
import javax.sound.sampled.LineEvent.Type.STOP
import worker.SoundClip
import worker.Logging

object MainAudio extends Logging {

  def main( args : Array[ String ] ) : Unit = {

    SoundClip( "sound/success.wav" ).play

  }

  def mainXXX( args : Array[ String ] ) : Unit = {

    println( "start" )

    import java.net.URL
    import javax.sound.sampled._

    val url = getClass.getResource( "sound/success.wav" )
    println( "url=" + url )

    val input = AudioSystem.getAudioInputStream( url )
    println( "format=" + input.getFormat() )

    val clip = AudioSystem.getClip

    val mixerList = AudioSystem.getMixerInfo
    for { mixer <- mixerList } {
      println( s"mixer=${mixer}" )
    }

    class AudioListener extends LineListener {
      def update( event : LineEvent ) : Unit = {
        import LineEvent.Type._
        event.getType() match {
          case OPEN  => println( "event=open" )
          case START => println( "event=start" )
          case STOP  => println( "event=stop" )
          case CLOSE => println( "event=close" )
          case _     => println( "event=xxx" )
        }
      }
    }

    val listener = new AudioListener

    clip.addLineListener( listener )

    clip.open( input )

    val control =
      clip.getControl( FloatControl.Type.MASTER_GAIN )
        .asInstanceOf[ FloatControl ]

    control.setValue( +5.0F )

    clip.start

    Thread.sleep( 5 * 1000 )

    clip.close

    println( "finish" )

  }

}