package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullDAO {

  /**
   * Gets a ChanNull by ID
   *
   * @param id  The ID of the ChanNull
   * @return
   */
  def get(id: UUID): Future[Option[ChanNull]]

  /**
   * Gets a ChanNull by name
   *
   * @param name  The name of the ChanNull
   * @return
   */
  def get(name: String): Future[Option[ChanNull]]

  /**
   * Search for ChanNulls by name contents
   *
   * @param nameContains  Name contents to search by
   * @param page          The page of the output
   * @param pageSize      The page size of the output
   * @return
   */
  def search(nameContains: String, page: Int, pageSize: Int): Future[Page[ChanNull]]

  /**
   * Gets a random public ChanNull
   *
   * @return
   */
  def getRandomPublic: Future[Option[ChanNull]]

  /**
   * Upserts a ChanNull
   *
   * @param request The request to upsert
   * @return
   */
  def upsert(request: UpsertChanNullRequest): Future[ChanNull]
}
