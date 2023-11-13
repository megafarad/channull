package net.channull.models.services

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo

import java.util.UUID
import io.github.honeycombcheesecake.play.silhouette.api.services.IdentityService
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CommonSocialProfile
import net.channull.models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   * @param id         The ID to retrieve a user.
   * @param providerID The ID of login info provider.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Creates or updates a user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   * @param loginInfo       social profile
   * @param email           user email
   * @param providedHandle  provided handle - method generates a random handle for user creation
   * @param firstName       first name
   * @param lastName        last name
   * @param fullName        full name
   * @param avatarURL       avatar URL
   * @return
   */
  def createOrUpdate(
    loginInfo: LoginInfo,
    email: String,
    providedHandle: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    avatarURL: Option[String]): Future[User]

}
