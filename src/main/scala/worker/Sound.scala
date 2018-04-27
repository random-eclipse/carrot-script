package worker

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineListener
import javax.sound.sampled.LineEvent
import javax.sound.sampled.FloatControl
import javax.sound.sampled.BooleanControl

object Sound {

}

class SoundListener extends LineListener {
  import LineEvent.Type._
import worker.SoundListener;
  private var event: LineEvent.Type = _
  def update(event: LineEvent): Unit = {
    //    event.getType() match {
    //      case OPEN => println("event=open")
    //      case START => println("event=start")
    //      case STOP => println("event=stop")
    //      case CLOSE => println("event=close")
    //      case _ => println("event=xxx")
    //    }
    this.event = event.getType
  }
  def isDone = event == STOP
  def awaitDone = while (!isDone) Thread.sleep(100)
}

case class SoundClip(path: String) extends Logging {

  def play = {

    val mixerList = AudioSystem.getMixerInfo
    for { mixer <- mixerList } {
      logger.debug(s"mixer: ${mixer}")
    }

    val url = getClass.getResource(path)
    val input = AudioSystem.getAudioInputStream(url)
    val listener = new SoundListener
    val clip = AudioSystem.getClip
    clip.addLineListener(listener)
    clip.open(input)

    val controlList = clip.getControls
    for { control <- controlList } {
      logger.debug(s"control: ${control}")
    }

    val controlGain =
      clip.getControl(FloatControl.Type.MASTER_GAIN)
        .asInstanceOf[FloatControl]
    controlGain.setValue(+5.0F)

    val controlMute =
      clip.getControl(BooleanControl.Type.MUTE)
        .asInstanceOf[BooleanControl]
    controlMute.setValue(false)

    val thread = new Thread(s"sound clip=${path}") {
      override def run = {
        try {
          logger.debug(s"Clip ${path}")
          clip.start
          listener.awaitDone
          Thread.sleep(50)
          clip.close
          logger.debug(s"Clip success.")
        } catch {
          case e: Throwable => logger.error("Clip failure.", e)
        }
      }
    }

    thread.start

    //    thread.join

  }

}

object SoundClip {

  def success = SoundClip("sound/success.wav").play  

  def failure = SoundClip("sound/failure.wav").play  
  
}
