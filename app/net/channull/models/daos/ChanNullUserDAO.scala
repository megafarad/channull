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
   * Deletes the ChanNullUser with the specified ID.
   *
   * @param id The UUID of the item to delete.
   * @return A Future[Unit] representing the completion of the delete operation.
   */
  def delete(id: UUID): Future[Unit]

}
