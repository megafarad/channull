package net.channull.models.services.captcha

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

trait CaptchaService {
  def validate(response: String, remoteIp: String): Future[Boolean]
}

class ReCaptchaService @Inject() (config: ReCaptchaConfig, ws: WSClient)(implicit ec: ExecutionContext)
  extends CaptchaService {
  def validate(recaptchaResponse: String, remoteIp: String): Future[Boolean] = ws
    .url("https://www.google.com/recaptcha/api/siteverify")
    .withHttpHeaders("Accept" -> "application/json")
    .withQueryStringParameters(
      "secret" -> config.secretKey,
      "response" -> recaptchaResponse,
      "remoteip" -> remoteIp
    )
    .get()
    .map(r => r.json.as[ReCaptchaValidationResponse])
    .map { r =>
      val e = r.errors.getOrElse(Vector.empty)
      if (e.isEmpty) {
        r.success
      } else {
        throw new Exception("Failed to retrieve reCaptcha confirmed response: " + e.mkString(";"))
      }
    }
}

case class ReCaptchaConfig(secretKey: String)

private[captcha] case class ReCaptchaValidationResponse(success: Boolean, errors: Option[Vector[String]])

private[captcha] object ReCaptchaValidationResponse {
  implicit val reads: Reads[ReCaptchaValidationResponse] = (
    (JsPath \ "success").read[Boolean] and
    (JsPath \ "error-codes").readNullable[Vector[String]]
  )(ReCaptchaValidationResponse.apply _)
}