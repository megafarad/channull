package net.channull.controllers

import java.util.UUID
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CredentialsProvider
import net.channull.forms.ResetPasswordForm

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Request }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Reset Password` controller.
 */
class ResetPasswordController @Inject() (
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Validates a password reset token.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def get(token: UUID): Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).map {
      case Some(_) => Ok(Json.obj("info" -> "Valid password reset token"))
      case None => BadRequest(Json.obj("error" -> Messages("invalid.reset.link")))
    }
  }

  /**
   * Resets the password.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def post(token: UUID): Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) =>
        ResetPasswordForm.form.bindFromRequest().fold(
          _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
          password => userService.retrieveUserLoginInfo(authToken.userID, CredentialsProvider.ID).flatMap {
            case Some((_, loginInfo)) =>
              val passwordInfo = passwordHasherRegistry.current.hash(password)
              authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
                Ok(Json.obj("success" -> Messages("password.reset")))
              }
            case _ => Future.successful(Forbidden(Json.obj("error" -> Messages("invalid.reset.link"))))
          }
        )
      case None => Future.successful(Forbidden(Json.obj("error" -> Messages("invalid.reset.link"))))
    }
  }
}
