
import worker.BaseConfig
import worker.Config._
import worker.EclipseMaven._
import worker.VersionPlugin._
import worker.VersionTag

//object Config_Advance_Release extends BaseConfig("image/medal-gold.png") {
//  override def execute = {
//    Config_Update_To_Release.execute
//    Config_Publish_Version.execute
//  }
//}

//object Config_Advance_Snapshot extends BaseConfig("image/medal-gold.png") {
//  override def execute = {
//    Config_Update_To_Snapshot.execute
//    Config_Publish_Version.execute
//  }
//}

object Config_Advance extends BaseConfig( "image/medal-gold.png" ) {
  override def execute = {
    Config_Resolve_Dependency.execute
    Config_Publish_Version.execute
  }
}

object Config_Publish_Version extends BaseConfig( "image/medal-gold.png" ) {
  override def execute = configContextList.foreach { context =>
    context.update
  }
}

object Config_Resolve_Dependency extends BaseConfig( "image/medal-gold.png" ) {
  override def execute = {
    configModelList.foreach { model =>
      mavenEnsure( model, "package -P resolve,!flatten" )
    }
  }
}

//object Config_Update_To_Release extends BaseConfig("image/medal-gold.png") {
//  override def execute = configModelList.foreach { model =>
//    // no parent change
//    //    if (model.getParent != null) {
//    //      mavenEnsure(model, versionUpdateParentToRelease(model).build)
//    //    }
//    mavenEnsure(model, versionUpdateDependenciesToRelease(model).build)
//  }
//}

//object Config_Update_To_Snapshot extends BaseConfig("image/medal-gold.png") {
//  override def execute = configModelList.foreach { model =>
//    // no parent change
//    //    if (model.getParent != null) {
//    //      mavenEnsure(model, versionUpdateParentToSnapshot(model).build)
//    //    }
//    mavenEnsure(model, versionUpdateDependenciesToSnapshot(model).build)
//  }
//}

object Config_Validate_Build extends BaseConfig( "image/medal-gold.png" ) {
  override def execute = {
    configContextList.foreach { context =>
      logger.info( s"Master config\n${context.masterText}" )
      logger.info( s"Version config\n${context.versionText}" )
    }
    configModelList.foreach { model =>
      mavenEnsure( model, "validate" )
    }
  }
}

object Config_Version_Tag_Cleanup extends BaseConfig( "image/medal-gold.png" ) {
  override def execute = {
    configContextList.foreach { context =>
      cleanupVersionTag( context )
    }
  }
}
