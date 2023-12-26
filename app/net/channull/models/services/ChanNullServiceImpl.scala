package net.channull.models.services

import net.channull.models._
import net.channull.models.daos._

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChanNullServiceImpl @Inject() (
  chanNullDAO: ChanNullDAO,
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

  /**
   * Gets a ChanNull by ID
   *
   * @param id The ID of the ChanNull
   * @return The ChanNull, if one exists with the given ID
   */
  def getChanNull(id: UUID): Future[Option[ChanNull]] = chanNullDAO.get(id)

  /**
   * Gets a ChanNull by name
   *
   * @param name The name of the ChanNull
   * @return The ChanNull, if one exists with the given name
   */
  def getChanNull(name: String): Future[Option[ChanNull]] = chanNullDAO.get(name)

  /**
   * Search for ChanNulls by name contents
   *
   * @param nameContains Name contents to search by
   * @param page         The page of the output
   * @param pageSize     The page size of the output
   * @return A paginated list of ChanNulls
   */
  def searchChanNulls(nameContains: String, page: Int, pageSize: Int): Future[Page[ChanNull]] =
    chanNullDAO.search(nameContains, page, pageSize)

  /**
   * Gets a random public ChanNull
   *
   * @return A random public ChanNull if it exists
   */
  def getRandomPublicChanNull: Future[Option[ChanNull]] = chanNullDAO.getRandomPublic

  /**
   * Upserts a ChanNull
   *
   * @param request The request to upsert
   * @return The upserted ChanNull
   */
  def upsertChanNull(request: UpsertChanNullRequest): Future[ChanNull] = chanNullDAO.upsert(request)
}
