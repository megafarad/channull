package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait BlockedUserDAO {

  /**
   * Gets blocks by blocking user ID
   *
   * @param blockingUserId  The ID of the blocking user.
   * @return
   */
  def getBlocks(blockingUserId: UUID): Future[Seq[BlockedUser]]

  /**
   * Upserts a blocked user
   *
   * @param request The request to upsert
   * @return
   */
  def upsert(request: UpsertBlockedUserRequest): Future[BlockedUser]

}
