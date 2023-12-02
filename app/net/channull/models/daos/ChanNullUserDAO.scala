package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullUserDAO {

  def getChanNullUsers(chanNullId: UUID): Future[Seq[ChanNullUser]]

  def upsert(request: UpsertChanNullUserRequest): Future[ChanNullUser]

  def delete(id: UUID): Future[Unit]

}