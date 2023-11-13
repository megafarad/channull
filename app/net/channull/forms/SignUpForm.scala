package net.channull.forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign up process.
 */
object SignUpForm {

  private def validateHandle(handle: String): Boolean = {
    handle.matches("^[A-Za-z0-9]*$")
  }
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "handle" -> nonEmptyText.verifying(handle => validateHandle(handle)),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText,
      "g-recaptcha-response" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param handle The user's handle.
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
   * @param email The email of the user.
   * @param password The password of the user.
   * @param captchaResponse The response from reCaptcha.
   */
  case class Data(
    handle: String,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    captchaResponse: String)
}
