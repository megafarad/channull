package net.channull.controllers

import io.github.honeycombcheesecake.play.silhouette.api._
import io.github.honeycombcheesecake.play.silhouette.api.exceptions.ProviderException
import io.github.honeycombcheesecake.play.silhouette.impl.exceptions.IdentityNotFoundException
import io.github.honeycombcheesecake.play.silhouette.impl.providers._
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import net.channull.forms.{ TotpForm, TotpSetupForm }
import net.channull.models.services.AuthenticateService

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `TOTP` controller.
 */
class TotpController @Inject() (
  scc: SilhouetteControllerComponents,
  authenticateService: AuthenticateService
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  implicit val passwordInfoFormat: Format[PasswordInfo] = Json.format[PasswordInfo]
  implicit val totpInfoFormat: Format[GoogleTotpInfo] = Json.format[GoogleTotpInfo]
  implicit val totpSetupFormat: Format[TotpSetupForm.Data] = Json.format[TotpSetupForm.Data]

  /**
   * Gets current TOTP setup
   * @return The result to display.
   */
  def getTotpSetup: Action[AnyContent] = SecuredAction.async { implicit request =>
    val user = request.identity
    val credentials = totpProvider.createCredentials(user.email.get, Some("ChanNull"))
    val totpInfo = credentials.totpInfo

    authenticateService.getAuthenticationProviders(user.email.get).flatMap {
      case providers if providers.contains(CredentialsProvider.ID) =>
        userService.retrieveUserLoginInfo(user.userID, GoogleTotpProvider.ID).flatMap {
          case Some((_, totpLoginInfo)) => authInfoRepository.find[GoogleTotpInfo](totpLoginInfo).map {
            case Some(value) => Ok(Json.obj("user" -> user, "totpInfo" -> value))
            case None => Ok(Json.obj(
              "user" -> user,
              "totpSetup" -> TotpSetupForm.Data(totpInfo.sharedKey, totpInfo.scratchCodes, credentials.scratchCodesPlain),
              "qrUrl" -> credentials.qrUrl
            ))
          }
          case None => Future.successful(Ok(Json.obj(
            "user" -> user,
            "totpSetup" -> TotpSetupForm.Data(totpInfo.sharedKey, totpInfo.scratchCodes, credentials.scratchCodesPlain),
            "qrUrl" -> credentials.qrUrl
          )))
        }
      case _ => Future.successful(NotFound(Json.obj("error" -> Messages("unsupported.login.method"))))
    }
  }

  /**
   * Disable TOTP.
   * @return The result to display.
   */
  def disableTotp: Action[AnyContent] = SecuredAction.async { implicit request =>
    val user = request.identity
    userService.retrieveUserLoginInfo(user.userID, GoogleTotpProvider.ID) flatMap {
      case Some((_, loginInfo)) =>
        authenticateService.removeAuthenticateMethod[GoogleTotpInfo](user.userID, loginInfo) map {
          _ => Ok(Json.obj("info" -> Messages("totp.disabling.info")))
        }
      case _ => Future.successful(NotFound(Json.obj("error" -> Messages("unsupported.login.method"))))
    }

  }

  /**
   * Handles the submitted form with TOTP initial data.
   * @return The result to display.
   */
  def postTotpSetup: Action[AnyContent] = SecuredAction.async { implicit request =>
    val user = request.identity
    TotpSetupForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
      data => {
        totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
          case Some(loginInfo: LoginInfo) => {
            authenticateService.addAuthenticateMethod(user.userID, loginInfo,
              GoogleTotpInfo(data.sharedKey, data.scratchCodes)) map {
                _ => Ok(Json.obj("success" -> Messages("totp.enabling.info")))
              }
          }
          case _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.verification.code"))))
        }.recover {
          case _: ProviderException =>
            BadRequest(Json.obj("error" -> Messages("invalid.unexpected.totp")))
        }
      }
    )
  }

  /**
   * Handles the submitted form with TOTP verification key.
   * @return The result to display.
   */
  def submit: Action[AnyContent] = UnsecuredAction.async { implicit request =>
    TotpForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
      data => {
        userService.retrieveUserLoginInfo(data.userID, CredentialsProvider.ID).flatMap {
          case Some((user, loginInfo)) =>
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
              case Some(_) => authenticateUser(user, loginInfo, data.rememberMe)
              case _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.verification.code"))))
            }.recover {
              case _: ProviderException =>
                BadRequest(Json.obj("error" -> Messages("invalid.unexpected.totp")))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }
}
