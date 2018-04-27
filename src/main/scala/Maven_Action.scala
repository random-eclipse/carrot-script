import worker.BaseMaven
import worker.EclipseMaven._
import worker.Release._
import worker.VersionPlugin._
import worker.ModelCache
import worker.model.MavenVersion
import worker.SoundClip

object Basic_Clean extends BaseMaven( "image/archive-remove.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "clean " + optsNoTest ) }
}

object Basic_Deploy extends BaseMaven( "image/arrow-up-3.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "deploy " + optsNoTest ) }
}

object Basic_Deploy_Clean extends BaseMaven( "image/arrow-up-3.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "clean deploy " + optsNoTest ) }
}

//object Basic_Deploy_with_Sources extends BaseMaven("image/arrow-up-double-3.png") {
//  override def execute = selectedMavenModelEach { mavenEnsure(_, "deploy -P attach-sources " + optsNoTest) }
//}

object Basic_Install extends BaseMaven( "image/arrow-right.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "install -P attach-sources " + optsNoTest ) }
}

object Basic_Install_Clean extends BaseMaven( "image/arrow-right-double.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "clean install -P attach-sources " + optsNoTest ) }
}

object Basic_Package extends BaseMaven( "image/arrow-in.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "package " + optsNoTest ) }
}

//object Basic_Package_Dump extends BaseMaven( "image/arrow-in.png" ) {
//  override def execute = selectedMavenModelEach { mavenEnsure( _, "package" ) }
//}

object Basic_Package_Clean extends BaseMaven( "image/arrow-inout.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "clean package " + optsNoTest ) }
}

object Basic_Test extends BaseMaven( "image/emblem-question.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "test" ) }
}

object Basic_Test_Clean extends BaseMaven( "image/emblem-question.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "clean test" ) }
}

object Basic_Test_Invoke extends BaseMaven( "image/emblem-question.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "verify -P invoke" ) }
}

object Basic_Test_Verify extends BaseMaven( "image/emblem-question.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "verify -P verify" ) }
}

object Basic_Release extends BaseMaven( "image/package-installed-updated.png" ) {
  override def enable = super.enable && selectedMavenModelList.forall { model =>
    val entry = mavenEntry( model.getPomFile )
    val canPerformRelease = entry.canPerformRelease
    canPerformRelease
  }
  override def execute = selectedMavenModelEach { model =>
    val entry = mavenEntry( model.getPomFile )
    runnerRelease( entry )
  }
}

object Basic_Update extends BaseMaven( "image/dialog-question-2.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "validate dependency:sources -U -V" ) }
}

object Basic_Validate extends BaseMaven( "image/dialog-question-2.png" ) {
  override def execute = selectedMavenModelEach { mavenEnsure( _, "validate -V" ) }
}

object Cascade_Release extends BaseMaven( "image/emblem-favorite.png" ) {
  override def enable = { super.enable && selectedMavenModelList.size == 1 }
  override def execute = {
    runnerCascade( mavenEntry( selectedMavenModelList( 0 ).getPomFile ) )
    SoundClip( "sound/success.wav" ).play
  }
}

object Version_All_To_Release extends BaseMaven( "image/Button-Blank-Blue-icon.png" ) {
  override def execute = {
    Version_Parent_To_Release.execute
    Version_Dependencies_To_Release.execute
  }
}

object Version_All_To_Snapshot extends BaseMaven( "image/Button-Blank-Red-icon.png" ) {
  override def execute = {
    Version_Parent_To_Snapshot.execute
    Version_Dependencies_To_Snapshot.execute
  }
}

object Version_Dependencies_To_Release extends BaseMaven( "image/group-blue-16.png" ) {
  override def execute = selectedMavenModelEach { model => mavenEnsure( model, versionUpdateDependenciesToRelease( model ).build ) }
}

object Version_Dependencies_To_Snapshot extends BaseMaven( "image/group-red-16.png" ) {
  override def execute = selectedMavenModelEach { model => mavenEnsure( model, versionUpdateDependenciesToSnapshot( model ).build ) }
}

object Version_Parent_To_Release extends BaseMaven( "image/User-Blue-icon.png" ) {
  override def execute = selectedMavenModelEach { model => mavenEnsure( model, versionUpdateParentToRelease( model ).build ) }
}

object Version_Parent_To_Snapshot extends BaseMaven( "image/User-Red-icon.png" ) {
  override def execute = selectedMavenModelEach { model => mavenEnsure( model, versionUpdateParentToSnapshot( model ).build ) }
}

object Version_Report_Updates extends BaseMaven( "image/dialog-question-2.png" ) {
  override def execute = selectedMavenModelEach { model => mavenEnsure( model, versionReportUpdates( model ).build ) }
}

object Version_To_This extends BaseMaven( "image/emblem-special.png" ) {
  override def execute = selectedMavenModelEach { selectedModel =>
    ModelCache.cacheEntrySetWithDependency( selectedModel ).foreach { discoveredEntry =>
      val versionOld = MavenVersion( discoveredEntry.dependencyVersion( selectedModel ).get )
      val versionNew = MavenVersion( selectedModel.getVersion )
      if ( versionOld == versionNew ) {
        logger.info( s"Already updated: ${discoveredEntry}" )
      } else {
        logger.info( s"Applying update: ${discoveredEntry}" )
        val command = versionSet( selectedModel, versionOld, versionNew,
                                  processDependencies = true, allowSnapshots = true ).build
        mavenEnsure( discoveredEntry.model, command )
      }
    }
  }
}

object Verify_Model_Cache extends BaseMaven( "image/dialog-question.png" ) {
  override def execute = {
    val cache = ModelCache.buildCache
    //cache.foreach { model => logger.info(s"   ${model}") }
  }
}

object Version_Action extends BaseMaven( "image/dialog-question.png" ) {
  import worker.CacheEntry

  case object EntrySort extends Ordering[ CacheEntry ] {
    def compare( x : CacheEntry, y : CacheEntry ) : Int = {
      val x1 = x.model.getGroupId + x.model.getArtifactId
      val y1 = y.model.getGroupId + y.model.getArtifactId
      x1.compareTo( y1 )
    }
  }

  override def execute = {

    val group = "com.carrotgarden"

    val cache = ModelCache.buildCache

    val carrot = cache.toList
      .filter { entry => entry.model.getGroupId.startsWith( group ) }
      .sorted( EntrySort )

    carrot.foreach { entry => logger.info( s"   ${entry}" ) }
  }
}
