package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullDAO {

  def getChanNull(id: UUID): Future[Option[ChanNull]]

  def getChanNull(name: String): Future[Option[ChanNull]]

  def saveChanNull(chanNull: ChanNull): Future[ChanNull]
}
