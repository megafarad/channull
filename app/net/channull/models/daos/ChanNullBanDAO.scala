package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullBanDAO {

  /**
   *
   * Gets bans by ChanNull ID
   *
   * @param chanNullId  The ID of the ChanNull
   * @return
   */
  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullBan]]

  /**
   *
   * Upserts a ChanNull Ban
   *
   * @param request   The request to upsert
   * @return
   */
  def upsert(request: UpsertChanNullBanRequest): Future[ChanNullBan]

  /**
   *
   * Deletes a ChanNull Ban
   *
   * @param id The ID of the ban
   * @return
   */
  def delete(id: UUID): Future[Unit]

}
