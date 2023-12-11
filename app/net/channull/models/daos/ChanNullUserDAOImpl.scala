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
  private case class ByChanNullAndUserId(userId: UUID, chanNullId: UUID) extends ChanNullUserQuery
  private case class ByChanNullId(chanNullId: UUID, page: Int, pageSize: Int) extends ChanNullUserQuery
  private def queryChanNullUsers(query: ChanNullUserQuery): Query[(ChanNullUserTable, UserTable), (ChanNullUserRow, User), Seq] = {
    val baseQuery = query match {
      case ByChanNullAndUserId(userId, chanNullId) => chanNullUserTableQuery.filter(tbl => tbl.userId === userId && tbl.chanNullId === chanNullId)
      case ByChanNullId(chanNullId, page, pageSize) => chanNullUserTableQuery.filter(_.chanNullId === chanNullId)
        .sortBy(_.role)
        .drop(page * pageSize).take(pageSize)
    }
    baseQuery.join(userTableQuery).on(_.userId === _.id)
  }

  private def runQueryChanNullUsers(query: ChanNullUserQuery) = db.run(queryChanNullUsers(query).result).map {
    chanNullUsers =>
      chanNullUsers map {
        case (chanNullUserFields, user) => ChanNullUser(chanNullUserFields.chanNullId, user, chanNullUserFields.role,
          chanNullUserFields.lastReadMessageId)
      }
  }

  private def upsertAction(request: UpsertChanNullUserRequest) = chanNullUserTableQuery.insertOrUpdate(
    ChanNullUserRow(request.chanNullId, request.userId, request.role, request.lastReadMessageId)
  )

  private def deleteAction(userId: UUID, chanNullId: UUID) = chanNullUserTableQuery.filter(tbl => tbl.userId === userId && tbl.chanNullId === chanNullId).delete

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
    _ => runQueryChanNullUsers(ByChanNullAndUserId(request.userId, request.chanNullId)).map(_.head)
  }

  /**
   * Deletes a user with the given userId and ChanNull with the given chanNullId.
   *
   * @param userId     The UUID of the user to be deleted.
   * @param chanNullId The UUID of the ChanNull to be deleted.
   * @return A Future that completes when the deletion is successful.
   */
  def delete(userId: UUID, chanNullId: UUID): Future[Unit] = db.run(deleteAction(userId, chanNullId)).map(_ => ())
}
