package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullUserDAO {

  /**
   * Gets a paginated list of users for a ChanNull
   *
   * @param chanNullId  The ID of the ChanNull
   * @param page        The page of the output
   * @param pageSize    The page size of the output
   * @return
   */
  def getChanNullUsers(chanNullId: UUID, page: Int, pageSize: Int): Future[Page[ChanNullUser]]

  /**
   * Upserts the ChanNullUser using the provided request.
   *
   * @param request The UpsertChanNullUserRequest object containing the information to upsert the ChanNullUser.
   * @return A Future of ChanNullUser representing the upserted ChanNullUser.
   */
  def upsert(request: UpsertChanNullUserRequest): Future[ChanNullUser]

  /**
   * Deletes a user with the given userId and ChanNull with the given chanNullId.
   *
   * @param userId     The UUID of the user to be deleted.
   * @param chanNullId The UUID of the ChanNull to be deleted.
   * @return A Future that completes when the deletion is successful.
   */
  def delete(userId: UUID, chanNullId: UUID): Future[Unit]

}
