package net.channull.models.services

import net.channull.models.daos._

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullServiceImpl @Inject() (
  chanNullPostDAO: ChanNullPostDAO,
  chanNullBanDAO: ChanNullBanDAO)(implicit ec: ExecutionContext) extends ChanNullService {

  /**
   * Deletes expired posts
   *
   * @return IDs of deleted posts
   */
  def cleanPosts: Future[Seq[UUID]] = chanNullPostDAO.findExpired(Instant.now()).flatMap {
    expiredPosts =>
      Future.sequence(expiredPosts.map {
        postId => chanNullPostDAO.delete(postId).map(_ => postId)
      })
  }

  /**
   * Deletes expired bans
   *
   * @return IDs of deleted bans
   */
  def cleanBans: Future[Seq[UUID]] = chanNullBanDAO.findExpired(Instant.now()).flatMap {
    expiredBans =>
      Future.sequence(expiredBans.map {
        banId => chanNullBanDAO.delete(banId).map(_ => banId)
      })
  }
}
