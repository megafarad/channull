package net.channull.models.daos

import net.channull.models._
import PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BlockedUserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends BlockedUserDAO with DAOSlick {

  private sealed trait BlockedUserQuery
  private case class ById(id: UUID) extends BlockedUserQuery
  private case class ByBlockingUserId(blockingUserId: UUID) extends BlockedUserQuery

  private def blockRowsQuery(query: BlockedUserQuery) = {
    val filteredQuery = query match {
      case ById(id) => blockedUserTableQuery.filter(_.id === id)
      case ByBlockingUserId(blockingUserId) => blockedUserTableQuery.filter(_.blockingUserId === blockingUserId)
    }
    filteredQuery.join(userTableQuery).on(_.blockingUserId === _.id).join(userTableQuery).on(_._1.blockedUserId === _.id)
      .map {
        case ((blockedUserTable, blockingUser), blockedUser) =>
          (blockedUserTable.id, blockingUser, blockedUser, blockedUserTable.timestamp)
      }
  }

  private def getBlocks(query: BlockedUserQuery) = db.run(blockRowsQuery(query).result).map {
    rows =>
      rows.map {
        case (id, blockingUser, blockedUser, timestamp) =>
          BlockedUser(id, blockingUser, blockedUser, timestamp)
      }
  }
  def getBlocks(blockingUserId: UUID): Future[Seq[BlockedUser]] = getBlocks(ByBlockingUserId(blockingUserId))

  def upsert(request: UpsertBlockedUserRequest): Future[BlockedUser] = {
    val upsertRowAction = blockedUserTableQuery.insertOrUpdate(BlockedUserRow(
      request.id, request.blockingUserId, request.blockedUserId, request.timestamp
    ))
    db.run(upsertRowAction).flatMap {
      _ => getBlocks(ById(request.id)).map(_.head)
    }
  }
}
