package worker

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import org.eclipse.jgit.api.ResetCommand
import Eclipse._
import org.eclipse.core.resources.IResource
import scala.collection.JavaConverters._
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.api.RebaseCommand
import org.eclipse.jgit.api.RebaseCommand._
import org.eclipse.jgit.lib.RebaseTodoLine
import java.util.function.Consumer
import org.eclipse.jgit.transport.TagOpt

object JGit extends Logging {

  def gitRepo(path: File) =
    new FileRepositoryBuilder().
      readEnvironment
      .setMustExist(true)
      .findGitDir(path)
      .build

  def gitAdd(path: File)(pattern: String) = Git.wrap(gitRepo(path)).add.addFilepattern(pattern).call

  def gitPull(path: File) = Git.wrap(gitRepo(path)).pull.call

  def gitPush(path: File)(remote: String = "origin", force: Boolean = false) =
    Git.wrap(gitRepo(path)).push.setRemote(remote).setForce(force) // no call

  def gitPushAll(path: File)(remote: String = "origin", force: Boolean = false) =
    Git.wrap(gitRepo(path)).push.setRemote(remote).setForce(force).setPushAll.call

  def gitPushTags(path: File)(remote: String = "origin", force: Boolean = false) =
    Git.wrap(gitRepo(path)).push.setRemote(remote).setForce(force).setPushTags.call

  def gitPushTotal(path: File)(remote: String = "origin", force: Boolean = false) =
    Git.wrap(gitRepo(path)).push.setRemote(remote).setForce(force).setPushAll.setPushTags.call

  def gitCommit(path: File)(message: String) = Git.wrap(gitRepo(path)).commit.setMessage(message).call

  def gitReset(path: File)(ref: String = "HEAD", mode: String = "HARD") =
    Git.wrap(gitRepo(path)).reset().setRef(ref).setMode(ResetCommand.ResetType.valueOf(mode))

  def gitTagCreate(path: File)(name: String, forceUpdate: Boolean = false) =
    Git.wrap(gitRepo(path)).tag.setName(name).setForceUpdate(forceUpdate).call

  def gitTagDelete(path: File)(tagList: String*) = {
    gitTagDeleteLocal(path)(tagList: _*)
    gitTagDeleteRemote(path)(tagList: _*)
  }

  def gitTagDeleteLocal(path: File)(tagList: String*) = {
    Git.wrap(gitRepo(path)).tagDelete().setTags(tagList: _*).call
  }

  def gitTagDeleteRemote(path: File)(tagList: String*) = {
    val push = gitPush(path)(force = true)
    tagList.foreach { name => push.add(":" + refsTags + name) }
    push.call
  }

  def gitTagList(path: File) = Git.wrap(gitRepo(path)).tagList.call

  def gitStatus(path: File) = Git.wrap(gitRepo(path)).status().call

  def gitRebase(path: File)(upstream: String, handler: InteractiveHandler) =
    Git.wrap(gitRepo(path)).rebase
      .setUpstream(upstream)
      .runInteractively(handler)
      .call

  def gitReflog(path: File) = Git.wrap(gitRepo(path)).reflog.call

  def gitGC(path: File) = Git.wrap(gitRepo(path)).gc.call

  def gitCheckout(path: File)(name: String, initHash: String = null, orphan: Boolean = false) =
    Git.wrap(gitRepo(path)).checkout
      .setName(name)
      .setOrphan(orphan)
      .setStartPoint(initHash)
      .call

  def gitFetch(path: File)(tagOpt: TagOpt = TagOpt.FETCH_TAGS, remote: String = "origin") =
    Git.wrap(gitRepo(path)).fetch
      .setTagOpt(tagOpt)
      .setRemote(remote)
      .call

  def gitBranchCreate(path: File)(name: String, hash: String = null) =
    Git.wrap(gitRepo(path)).branchCreate
      .setName(name)
      .setStartPoint(hash)
      .call

  def gitBranchDelete(path: File)(name: String, force: Boolean = false) =
    Git.wrap(gitRepo(path)).branchDelete
      .setBranchNames(name)
      .setForce(force)
      .call

  def gitBranchList(path: File)(name: String*) =
    Git.wrap(gitRepo(path)).branchList
      .call

  def gitBranchRename(path: File)(oldName: String, newName: String) =
    Git.wrap(gitRepo(path)).branchRename
      .setOldName(oldName)
      .setNewName(newName)
      .call

  def gitBranchCurrent(path: File) = gitRepo(path).getBranch

  //

  val git_dir = ".git"

  /** Relative path to tags references in GIT database. */
  val refsTags = "refs/tags/"

  /** List of root git repositories. */
  lazy val repoResourceList: List[IResource] = selectedResourceList
    .filter(resource => new File(resource.getLocation.toFile, git_dir).exists)

  lazy val repoFileList: List[File] = repoResourceList.map { resource => resource.getLocation.toFile }

  def enable = !repoResourceList.isEmpty

  def repoCreateTag(path: File, tag: String) = {
    logger.info(s"Path: ${path} Tag: ${tag}")
    gitPull(path)
    gitTagCreate(path)(tag, forceUpdate = true)
    gitPushTotal(path)()
  }

  def repoDeleteTag(path: File, pattern: String): Unit = {

    logger.info(s"Path: ${path} Pattern:${pattern}")

    logger.info(s"Git Pull.")
    gitPull(path)

    val regex = pattern.r

    logger.info(s"Git Tag List.")
    val tagList = gitTagList(path).asScala

    /** Find tags matching pattern. */
    val sourceTagList = for {
      tag <- tagList
      name = tag.getName.replaceAll(refsTags, "")
      find = regex.findFirstIn(name)
      if (find.isDefined)
    } yield {
      name
    }

    //    logger.info(s"Git Done.")
    //    return

    val targetTagList = sourceTagList.sorted

    val deleteTagList = targetTagList

    logger.info(s"Delete tags.")
    gitTagDelete(path)(deleteTagList: _*)

    logger.info(s"Deleted tag count=${deleteTagList.size}")

  }

  def tagName(tag: Ref) = tag.getName.replaceAll(refsTags, "")

  def repoTruncate(path: File, tag: String) = {

    val repo = gitRepo(path)
    val branch = gitBranchCurrent(path)
    val orphan = "orphan"

    val config = repo.getConfig
    val branchMerge = config.getString("branch", branch, "merge")
    val branchRemote = config.getString("branch", branch, "remote")

    logger.info(s"Path: ${path} Tag: ${tag} Branch: ${branch} Orphan: ${orphan}")

    logger.info(s"Fetch.")
    gitFetch(path)()

    logger.info(s"Pull.")
    gitPull(path)

    logger.info(s"Add.")
    gitAdd(path)(".")

    logger.info(s"Commit ${branch}.")
    gitCommit(path)(s"truncate-${branch}")

    logger.info(s"Push total.")
    gitPushTotal(path)()

    logger.info(s"Tag Delete.")
    val tagList = gitTagList(path).asScala
    val tagNameList = tagList.map { _.getName.replaceAll(refsTags, "") }
    gitTagDelete(path)(tagNameList: _*)

    logger.info(s"Delete ${orphan}.")
    gitBranchDelete(path)(orphan, true)

    logger.info(s"Checkout ${orphan}.")
    gitCheckout(path)(orphan, null, true)

    logger.info(s"Commit ${orphan}.")
    gitCommit(path)(s"archon-${branch}")

    logger.info(s"Delete ${branch}.")
    gitBranchDelete(path)(branch, true)

    logger.info(s"Rename ${orphan} -> ${branch}.")
    gitBranchRename(path)(orphan, branch)

    logger.info(s"Run GC ${branch}.")
    gitGC(path)

    logger.info(s"Config ${branch}.")
    config.setString("branch", branch, "merge", branchMerge)
    config.setString("branch", branch, "remote", branchRemote)
    config.save

    logger.info(s"Push ${branch}.")
    gitPushTotal(path)(force = true)

  }

}

    //    val tagRef = tagList.find { x => tagName(x) == tag }.get
    //    logger.info(s"Tag Ref: ${tagRef}.")
    //    val walk = new RevWalk(repo)
    //    val tagRev = walk.parseTag(tagRef.getObjectId)
    //    val date = tagRev.getTaggerIdent.getWhen
    //    logger.info(s"Tag Date: ${date}.")
    //    val tagCommHash = tagRev.getObject.getName
    //    logger.info(s"Tag commit hash: ${tagCommHash}.")
    //    val (tagListRemove, tagListPreserve) = tagList.partition {
    //      x => walk.parseTag(x.getObjectId).getTaggerIdent.getWhen.before(date)
    //    }
    //    logger.info(s"Tag Remove: \n${tagListRemove.mkString("\n")}")
    //    logger.info(s"Tag Preserve: \n${tagListPreserve.mkString("\n")}")
