package net.channull.modules

import com.google.inject.{ AbstractModule, Provides }
import net.channull.models.daos.{ AuthTokenDAO, AuthTokenDAOImpl }
import net.channull.models.services.captcha.{ CaptchaService, ReCaptchaConfig, ReCaptchaService }
import net.channull.models.services.{ AuthTokenService, AuthTokenServiceImpl }
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[CaptchaService].to[ReCaptchaService]
  }

  @Provides
  def providesReCaptchaConfig(conf: Configuration): ReCaptchaConfig = {
    ReCaptchaConfig(conf.get[String]("recaptcha.secretKey"))
  }
}
