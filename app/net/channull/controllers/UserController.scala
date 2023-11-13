package net.channull.controllers

import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.{ CredentialsProvider, GoogleTotpInfo, GoogleTotpProvider }
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class UserController @Inject() (scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext)
  extends AbstractAuthController(scc) {

  implicit val passwordInfoFormat: Format[PasswordInfo] = Json.format[PasswordInfo]
  implicit val totpInfoFormat: Format[GoogleTotpInfo] = Json.format[GoogleTotpInfo]

  def get: Action[AnyContent] = SecuredAction.async { implicit request =>
    userService.retrieveUserLoginInfo(request.identity.userID, GoogleTotpProvider.ID).flatMap {
      case Some((user, loginInfo)) =>
        authInfoRepository.find[GoogleTotpInfo](loginInfo).map {
          case Some(value) => Ok(Json.obj("user" -> Json.toJson(user), "totpInfo" -> Json.toJson(value)))
          case None => Ok(Json.obj("user" -> Json.toJson(user)))
        }
      case _ => Future.successful(Ok(Json.obj("user" -> Json.toJson(request.identity))))
    }
  }
}
