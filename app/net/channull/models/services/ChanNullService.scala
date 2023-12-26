package net.channull.models.services

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullService {

  /**
   * Deletes expired posts
   * @return IDs of deleted posts
   */
  def cleanPosts: Future[Seq[UUID]]

  /**
   * Deletes expired bans
   * @return IDs of deleted bans
   */
  def cleanBans: Future[Seq[UUID]]

  /**
   * Gets a ChanNull by ID
   *
   * @param id The ID of the ChanNull
   * @return The ChanNull, if one exists with the given ID
   */
  def getChanNull(id: UUID): Future[Option[ChanNull]]

  /**
   * Gets a ChanNull by name
   *
   * @param name  The name of the ChanNull
   * @return The ChanNull, if one exists with the given name
   */
  def getChanNull(name: String): Future[Option[ChanNull]]

  /**
   * Search for ChanNulls by name contents
   *
   * @param nameContains  Name contents to search by
   * @param page          The page of the output
   * @param pageSize      The page size of the output
   * @return              A paginated list of ChanNulls
   */
  def searchChanNulls(nameContains: String, page: Int, pageSize: Int): Future[Page[ChanNull]]

  /**
   * Gets a random public ChanNull
   *
   * @return A random public ChanNull if it exists
   */
  def getRandomPublicChanNull: Future[Option[ChanNull]]

  /**
   * Upserts a ChanNull
   *
   * @param request The request to upsert
   * @return  The upserted ChanNull
   */
  def upsertChanNull(request: UpsertChanNullRequest): Future[ChanNull]
}
