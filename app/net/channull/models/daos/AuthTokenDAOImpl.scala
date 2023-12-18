package net.channull.models.daos

import java.util.UUID
import java.time.Instant
import net.channull.models.AuthToken
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import PostgresProfile.api._

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends AuthTokenDAO with DAOSlick {
  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = db.run(authTokenTableQuery.filter(tbl => tbl.id === id && tbl.expiry
    >= Instant.now).result.headOption)

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: Instant): Future[Seq[AuthToken]] =
    db.run(authTokenTableQuery.filter(_.expiry < dateTime).result)

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] =
    db.run(authTokenTableQuery.insertOrUpdate(token).map(_ => token))

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit] =
    db.run(authTokenTableQuery.filter(_.id === id).delete.map(_ => ()))
}
