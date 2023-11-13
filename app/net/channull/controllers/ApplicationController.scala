package net.channull.controllers

import controllers.Assets
import io.github.honeycombcheesecake.play.silhouette.api.LogoutEvent
import io.github.honeycombcheesecake.play.silhouette.api.actions._
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * The basic application controller.
 */
class ApplicationController @Inject() (
  scc: SilhouetteControllerComponents,
  assets: Assets,
  errorHandler: HttpErrorHandler,
  config: Configuration
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index: Action[AnyContent] = assets.at("index.html")

  def assetOrDefault(resource: String): Action[AnyContent] =
    if (resource.startsWith(config.get[String]("apiPrefix"))) {
      Action.async(r => errorHandler.onClientError(r, NOT_FOUND, "Not found"))
    } else {
      if (resource.contains(".")) assets.at(resource) else index
    }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut: Action[AnyContent] = SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val result = Ok(Json.obj("info" -> "Logged out successfully"))
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
