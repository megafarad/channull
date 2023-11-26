package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullBanDAO {

  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullBan]]

  def upsert(request: UpsertChanNullBanRequest): Future[ChanNullBan]

  def delete(id: UUID): Future[Unit]

}
