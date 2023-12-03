package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait BlockedUserDAO {
  def getBlocks(blockingUserId: UUID): Future[Seq[BlockedUser]]

  def upsert(request: UpsertBlockedUserRequest): Future[BlockedUser]

}
