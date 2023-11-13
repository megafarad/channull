package net.channull.controllers

import java.util.UUID
import io.github.honeycombcheesecake.play.silhouette.api._
import io.github.honeycombcheesecake.play.silhouette.impl.providers._

import javax.inject.Inject
import net.channull.forms.SignUpForm
import net.channull.models.User
import net.channull.models.services.AuthenticateService
import net.channull.models.services.captcha.CaptchaService
import play.api.i18n.Messages
import play.api.libs.mailer.Email
import play.api.mvc._
import play.api.libs.json.Json

import java.time.{ Instant, ZoneId }
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign Up` controller.
 */
class SignUpController @Inject() (
  components: SilhouetteControllerComponents,
  authenticateService: AuthenticateService,
  captchaService: CaptchaService
)(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def post: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
      data => {
        captchaService.validate(data.captchaResponse, request.remoteAddress).flatMap {
          valid =>
            if (valid) {
              val result = Ok(Json.obj("info" -> Messages("sign.up.email.sent", data.email)))
              val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
              userService.retrieve(loginInfo).flatMap {
                case Some(user) =>
                  val url = getBaseUrl + "/signIn"
                  mailerClient.send(Email(
                    subject = Messages("email.already.signed.up.subject"),
                    from = Messages("email.from"),
                    to = Seq(data.email),
                    bodyText = Some(net.channull.views.txt.emails.alreadySignedUp(user, url).body),
                    bodyHtml = Some(net.channull.views.html.emails.alreadySignedUp(user, url).body)
                  ))

                  Future.successful(result)
                case None =>
                  val authInfo = passwordHasherRegistry.current.hash(data.password)
                  val user = User(
                    userID = UUID.randomUUID(),
                    handle = data.handle,
                    firstName = Some(data.firstName),
                    lastName = Some(data.lastName),
                    fullName = Some(data.firstName + " " + data.lastName),
                    email = Some(data.email),
                    avatarURL = None,
                    profile = None,
                    signedUpAt = Instant.now(),
                    activated = false
                  )
                  for {
                    avatar <- avatarService.retrieveURL(data.email)
                    user <- userService.save(user.copy(avatarURL = avatar))
                    _ <- authenticateService.addAuthenticateMethod(user.userID, loginInfo, authInfo)
                    authToken <- authTokenService.create(user.userID)
                  } yield {
                    val url = getBaseUrl(request) + "/account/activate/" + authToken.id
                    mailerClient.send(Email(
                      subject = Messages("email.sign.up.subject"),
                      from = Messages("email.from"),
                      to = Seq(data.email),
                      bodyText = Some(net.channull.views.txt.emails.signUp(user, url).body),
                      bodyHtml = Some(net.channull.views.html.emails.signUp(user, url).body)
                    ))

                    eventBus.publish(SignUpEvent(user, request))
                    result
                  }
              }
            } else {
              Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request"))))
            }
        }

      }
    )
  }

}
