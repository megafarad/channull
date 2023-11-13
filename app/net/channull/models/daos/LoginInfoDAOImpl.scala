package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import net.channull.models.User
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class LoginInfoDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends LoginInfoDAO with DAOSlick {

  import profile.api._
  /**
   * Finds a user and login info pair by userID and login info providerID
   *
   * @param userId     user id
   * @param providerId provider id
   * @return Some(User, LoginInfo) if there is a user by userId which has login method for provider by provider ID, otherwise None
   */
  def find(userId: UUID, providerId: String): Future[Option[(User, LoginInfo)]] = {
    val action = for {
      ((_, loginInfoTable), userTable) <- userLoginInfoTableQuery.filter(_.userID === userId)
        .join(loginInfoTableQuery).on(_.loginInfoID === _.id)
        .join(userTableQuery).on(_._1.userID === _.id)
      if loginInfoTable.providerID === providerId
    } yield (userTable, loginInfoTable)

    db.run(action.result.headOption)
      .map(_.map {
        case (user, loginInfoRow) => (user, LoginInfo(loginInfoRow.providerID, loginInfoRow.providerKey))
      })
  }

  /**
   * Saves a login info for user
   *
   * @param userID    The user id.
   * @param loginInfo login info
   * @return unit
   */
  def saveUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit] = {
    val loginInfoRow = LoginInfoRow(None, loginInfo.providerID, loginInfo.providerKey)

    val loginInfoAction = {
      val retrieveLoginInfo = loginInfoTableQuery.filter(
        info => info.providerID === loginInfo.providerID &&
          info.providerKey === loginInfo.providerKey
      ).result.headOption
      val insertLoginInfo = loginInfoTableQuery.returning(loginInfoTableQuery.map(_.id))
        .into((info, id) => info.copy(id = Some(id))) += loginInfoRow
      for {
        loginInfoOption <- retrieveLoginInfo
        savedLoginInfo <- loginInfoOption.map(DBIO.successful).getOrElse(insertLoginInfo)
      } yield savedLoginInfo
    }
    val actions = (for {
      loginInfoRow <- loginInfoAction
      userLoginInfo = UserLoginInfoRow(userID, loginInfoRow.id.get)
      exists <- userLoginInfoTableQuery.filter(e => e.loginInfoID === userLoginInfo.loginInfoID &&
        e.userID === userLoginInfo.userID).exists.result
      _ <- if (exists) DBIO.successful(()) else userLoginInfoTableQuery += userLoginInfo
    } yield ()).transactionally
    db.run(actions)
  }

  /**
   * Get list of user authentication methods providers
   *
   * @param email user email
   * @return
   */
  def getAuthenticationProviders(email: String): Future[Seq[String]] = {
    val action = for {
      ((_, _), li) <- userTableQuery.filter(_.email === email)
        .join(userLoginInfoTableQuery).on(_.id === _.userID)
        .join(loginInfoTableQuery).on(_._2.loginInfoID === _.id)
    } yield li.providerID

    db.run(action.result)
  }

  /**
   * Deletes login info for a user
   *
   * @param userID    The user ID
   * @param loginInfo Login Info
   * @return
   */
  def deleteUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit] = {
    val userLoginInfoRows = userLoginInfoTableQuery.filter(_.userID === userID).filter(_.loginInfoID in loginInfoQuery(loginInfo).map(_.id))
    val loginInfoRows = loginInfoQuery(loginInfo)
    db.run((userLoginInfoRows.delete andThen loginInfoRows.delete).transactionally).map(_ => ())
  }
}
