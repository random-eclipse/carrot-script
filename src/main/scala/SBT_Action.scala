
import worker.BaseSBT
import worker.Eclipse._
import com.jcraft.jsch.JSch
import java.io.File

object SBT_Clean extends BaseSBT( "image/archive-remove.png" ) {
  override def command = "clean"
}

object SBT_Compile extends BaseSBT( "image/arrow-right.png" ) {
  override def command = "compile"
}

object SBT_Compile_Test extends BaseSBT( "image/arrow-right-double.png" ) {
  override def command = "test:compile"
}

object SBT_Eclipse extends BaseSBT( "image/Button-Blank-Red-icon.png" ) {
  override def command = "eclipse"
}

object SBT_Test extends BaseSBT( "image/emblem-question.png" ) {
  override def command = "test"
}
