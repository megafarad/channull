package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullDAO {

  def get(id: UUID): Future[Option[ChanNull]]

  def get(name: String): Future[Option[ChanNull]]

  def getRandomPublic: Future[Option[ChanNull]]

  def save(chanNull: ChanNull): Future[ChanNull]
}
