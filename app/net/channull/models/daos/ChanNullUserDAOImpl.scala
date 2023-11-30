package net.channull.models.daos

import net.channull.models._
import PostgresProfile.api._
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChanNullUserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                                    (implicit ec: ExecutionContext) extends ChanNullUserDAO with DAOSlick with Logging {

  private sealed trait ChanNullUserQuery
  private case class ById(id: UUID) extends ChanNullUserQuery
  private case class ByChanNullId(chanNullId: UUID) extends ChanNullUserQuery
  private def queryChanNullUsers(query: ChanNullUserQuery): Query[(ChanNullUserTable, UserTable),
    (ChanNullUserRow, User), Seq] = {
    val baseQuery = query match {
      case ById(id) => chanNullUserTableQuery.filter(_.id === id)
      case ByChanNullId(chanNullId) => chanNullUserTableQuery.filter(_.chanNullId === chanNullId)
    }
    baseQuery.join(userTableQuery).on(_.userId === _.id)
  }

  private def runQueryChanNullUsers(query: ChanNullUserQuery) = db.run(queryChanNullUsers(query).result).map {
    chanNullUsers =>
      chanNullUsers map {
        case (chanNullUserFields, user) => ChanNullUser(chanNullUserFields.id, chanNullUserFields.chanNullId, user,
          chanNullUserFields.role)
      }
  }

  private def upsertAction(request: UpsertChanNullUserRequest) = chanNullUserTableQuery.insertOrUpdate(
    ChanNullUserRow(request.id, request.chanNullId, request.userId, request.role)
  )

  private def deleteAction(id: UUID) = chanNullUserTableQuery.filter(_.id === id).delete

  def getChanNullUsers(chanNullId: UUID): Future[Seq[ChanNullUser]] = runQueryChanNullUsers(ByChanNullId(chanNullId))

  def upsert(request: UpsertChanNullUserRequest): Future[ChanNullUser] = db.run(upsertAction(request)).flatMap {
    _ => runQueryChanNullUsers(ById(request.id)).map(_.head)
  }

  def delete(id: UUID): Future[Unit] = db.run(deleteAction(id)).map(_ => ())
}
