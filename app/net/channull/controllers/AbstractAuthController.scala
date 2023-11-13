package net.channull.controllers

import io.github.honeycombcheesecake.play.silhouette.api.Authenticator.Implicits._
import io.github.honeycombcheesecake.play.silhouette.api._
import io.github.honeycombcheesecake.play.silhouette.api.services.AuthenticatorResult
import net.channull.models.User
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * `AbstractAuthController` base with support methods to authenticate an user.
 *
 * @param scc The Silhouette stack.
 * @param ex The execution context.
 */
abstract class AbstractAuthController(
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Performs user authentication
   * @param user User data
   * @param rememberMe Remember me flag
   * @param request Initial request
   * @return The result to display.
   */
  protected def authenticateUser(user: User, loginInfo: LoginInfo, rememberMe: Boolean)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    val result = Ok(Json.obj("user" -> Json.toJson(user)))
    authenticatorService.create(loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + scc.rememberMeConfig.expiry,
          idleTimeout = scc.rememberMeConfig.idleTimeout,
          cookieMaxAge = scc.rememberMeConfig.cookieMaxAge
        )
      case authenticator => authenticator
    }.flatMap { authenticator =>
      eventBus.publish(LoginEvent(user, request))
      authenticatorService.init(authenticator).flatMap { v =>
        authenticatorService.embed(v, result)
      }
    }
  }
}
