
import worker.Base
import worker.Eclipse._
import worker.EclipseMaven._
import worker.SoundClip
import worker.BaseRepo
import worker.JGit._
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date

//object Repo_Status extends BaseRepo("image/emblem-generic.png") {
//  override def image = resourceStream("image/emblem-generic.png")
//  override def execute = {
//    repoFileList.foreach { path =>
//      val status = gitStatus(path)
//      logger.info(s"Status: ${status.getUntracked}")
//    }
//  }
//}

object Repo_Tag_Create extends BaseRepo( "image/emblem-generic.png" ) {

  val zone = TimeZone.getTimeZone( "UTC" );
  val form = new SimpleDateFormat( "'marker'_yyyy-MM-dd_HH-mm-ss" );
  form.setTimeZone( zone )
  val tag = form.format( new Date() )

  override def execute = {
    repoFileList.foreach { path =>
      repoCreateTag( path, tag )
    }
  }
}

object Repo_Tag_Delete extends BaseRepo( "image/emblem-generic.png" ) {

  val nothing = """^$"""
  val everyting = """^(.+)$"""
  val release_version = """^(.+)-(\d+\.\d+\.+\d+)$"""

  override def execute = {
    repoFileList.foreach { path =>
      val pattern = everyting
      repoDeleteTag( path, pattern )
    }
  }
}

object Repo_Truncate extends BaseRepo( "image/emblem-generic.png" ) {

  val tag = "invalid"

  override def execute = {
    repoFileList.foreach { path =>
      logger.info( s"Truncate." )
      repoTruncate( path, tag )
    }
    repoFileList.foreach { path =>
      logger.info( s"Refresh." )
      gitPull( path )
    }
  }
}
