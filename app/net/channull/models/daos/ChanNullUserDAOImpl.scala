package net.channull.models.daos

import net.channull.models._
import PostgresProfile.api._
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullUserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ChanNullUserDAO with DAOSlick with Logging {

  private sealed trait ChanNullUserQuery
  private case class ById(id: UUID) extends ChanNullUserQuery
  private case class ByChanNullId(chanNullId: UUID, page: Int, pageSize: Int) extends ChanNullUserQuery
  private def queryChanNullUsers(query: ChanNullUserQuery): Query[(ChanNullUserTable, UserTable), (ChanNullUserRow, User), Seq] = {
    val baseQuery = query match {
      case ById(id) => chanNullUserTableQuery.filter(_.id === id)
      case ByChanNullId(chanNullId, page, pageSize) => chanNullUserTableQuery.filter(_.chanNullId === chanNullId)
        .drop(page * pageSize).take(pageSize)
    }
    baseQuery.join(userTableQuery).on(_.userId === _.id)
  }

  private def runQueryChanNullUsers(query: ChanNullUserQuery) = db.run(queryChanNullUsers(query).result).map {
    chanNullUsers =>
      chanNullUsers map {
        case (chanNullUserFields, user) => ChanNullUser(chanNullUserFields.id, chanNullUserFields.chanNullId, user,
          chanNullUserFields.role, chanNullUserFields.lastReadMessageId)
      }
  }

  private def upsertAction(request: UpsertChanNullUserRequest) = chanNullUserTableQuery.insertOrUpdate(
    ChanNullUserRow(request.id, request.chanNullId, request.userId, request.role, request.lastReadMessageId)
  )

  private def deleteAction(id: UUID) = chanNullUserTableQuery.filter(_.id === id).delete

  /**
   * Gets a paginated list of users for a ChanNull
   *
   * @param chanNullId  The ID of the ChanNull
   * @param page        The page of the output
   * @param pageSize    The page size of the output
   * @return
   */
  def getChanNullUsers(chanNullId: UUID, page: Int, pageSize: Int): Future[Page[ChanNullUser]] = for {
    items <- runQueryChanNullUsers(ByChanNullId(chanNullId, page, pageSize))
    totalCount <- db.run(chanNullUserTableQuery.filter(_.chanNullId === chanNullId).length.result)
  } yield Page(items, page = page, offset = page * pageSize, total = totalCount.toLong)

  /**
   * Upserts the ChanNullUser using the provided request.
   *
   * @param request The UpsertChanNullUserRequest object containing the information to upsert the ChanNullUser.
   * @return A Future of ChanNullUser representing the upserted ChanNullUser.
   */
  def upsert(request: UpsertChanNullUserRequest): Future[ChanNullUser] = db.run(upsertAction(request)).flatMap {
    _ => runQueryChanNullUsers(ById(request.id)).map(_.head)
  }

  /**
   * Deletes the ChanNullUser with the specified ID.
   *
   * @param id The UUID of the item to delete.
   * @return A Future[Unit] representing the completion of the delete operation.
   */
  def delete(id: UUID): Future[Unit] = db.run(deleteAction(id)).map(_ => ())
}
