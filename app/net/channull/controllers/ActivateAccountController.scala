package net.channull.controllers

import java.net.URLDecoder
import java.util.UUID
import io.github.honeycombcheesecake.play.silhouette.api._
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CredentialsProvider

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.mailer.Email
import play.api.mvc.{ Action, AnyContent, Request }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Activate Account` controller.
 */
class ActivateAccountController @Inject() (
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Sends an account activation email to the user with the given email.
   *
   * @param email The email address of the user to send the activation mail to.
   * @return The result to display.
   */
  def send(email: String): Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    val decodedEmail = URLDecoder.decode(email, "UTF-8")
    val loginInfo = LoginInfo(CredentialsProvider.ID, decodedEmail)
    val result = Ok(Json.obj("info" -> Messages("activation.email.sent", decodedEmail)))

    userService.retrieve(loginInfo).flatMap {
      case Some(user) if !user.activated =>
        authTokenService.create(user.userID).map { authToken =>
          val url = getBaseUrl + "/account/activate/" + authToken.id

          mailerClient.send(Email(
            subject = Messages("email.activate.account.subject"),
            from = Messages("email.from"),
            to = Seq(decodedEmail),
            bodyText = Some(net.channull.views.txt.emails.activateAccount(user, url).body),
            bodyHtml = Some(net.channull.views.html.emails.activateAccount(user, url).body)
          ))
          result
        }
      case _ => Future.successful(result)
    }
  }

  /**
   * Activates an account.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def activate(token: UUID): Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieveUserLoginInfo(authToken.userID, CredentialsProvider.ID).flatMap {
        case Some((user, _)) =>
          userService.save(user.copy(activated = true)).map { _ =>
            Ok(Json.obj("success" -> Messages("account.activated")))
          }
        case _ => Future.successful(Forbidden(Json.obj("error" -> Messages("invalid.activation.link"))))
      }
      case None => Future.successful(Forbidden(Json.obj("error" -> Messages("invalid.activation.link"))))
    }
  }
}
