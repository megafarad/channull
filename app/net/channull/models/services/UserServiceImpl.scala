package net.channull.models.services

import java.util.UUID
import javax.inject.Inject
import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CommonSocialProfile
import net.channull.models.User
import net.channull.models.daos.{ LoginInfoDAO, UserDAO }

import java.time.{ Instant, ZoneId }
import scala.util.Random
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO, loginInfoDAO: LoginInfoDAO)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]] = userDAO.find(id)

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   * @param id         The ID to retrieve a user.
   * @param providerID The ID of login info provider.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]] =
    loginInfoDAO.find(id, providerID)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = userDAO.save(user)

  /**
   * Creates or updates a user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   * @param loginInfo social profile
   * @param email     user email
   * @param firstName first name
   * @param lastName  last name
   * @param avatarURL avatar URL
   * @return
   */
  def createOrUpdate(
    loginInfo: LoginInfo,
    email: String,
    providedHandle: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    avatarURL: Option[String]): Future[User] = {
    Future.sequence(Seq(userDAO.find(loginInfo), userDAO.findByEmail(email))).flatMap { users =>
      users.flatten.headOption match {
        case Some(user) => userDAO.save(user.copy(
          firstName = firstName,
          lastName = lastName,
          email = Some(email),
          avatarURL = avatarURL
        ))
        case None => userDAO.save(User(
          userID = UUID.randomUUID(),
          handle = providedHandle.getOrElse(generateHandle(firstName, lastName)),
          firstName = firstName,
          lastName = lastName,
          fullName = fullName,
          email = Some(email),
          avatarURL = avatarURL,
          profile = None,
          signedUpAt = Instant.now(),
          activated = false
        ))
      }
    }

  }

  private def generateHandle(firstName: Option[String], lastName: Option[String]): String = {
    if (firstName.isDefined || lastName.isDefined) {
      firstName.getOrElse("") + lastName.getOrElse("") + generateRandomDigits(6)
    } else {
      "u" + generateRandomDigits(18)
    }
  }

  private def generateRandomDigits(length: Int): String = {
    require(length > 0, "Length must be greater than zero.")
    val maxNumber: Long = Math.pow(10, length.toDouble).toLong
    val digits: Long = Random.nextLong(maxNumber)
    String.format(s"%0${length}d", digits)
  }

}
