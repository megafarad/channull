package net.channull.models

import java.util.UUID
import io.github.honeycombcheesecake.play.silhouette.api.{ Identity, LoginInfo }
import play.api.libs.json._

import java.time.Instant

/**
 * The user object.
 *
 * @param userID The unique ID of the user.
 * @param handle The user's handle.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param fullName Maybe the full name of the authenticated user.
 * @param email Maybe the email of the authenticated user.
 * @param avatarURL Maybe the avatar URL of the authenticated user.
 * @param profile Maybe the profile of the authenticated user.
 * @param signedUpAt The date/time when the user signed up.
 * @param activated Indicates that the user has activated its registration.
 */
case class User(
  userID: UUID,
  handle: String,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  profile: Option[String],
  signedUpAt: Instant,
  activated: Boolean) extends Identity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name: Option[String] = fullName.orElse {
    firstName -> lastName match {
      case (Some(f), Some(l)) => Some(f + " " + l)
      case (Some(f), None) => Some(f)
      case (None, Some(l)) => Some(l)
      case _ => None
    }
  }
}

object User {

  implicit val jsonFormat: Format[User] = Json.format[User]

}
