package net.channull.jobs

import javax.inject.Inject
import akka.actor._
import io.github.honeycombcheesecake.play.silhouette.api.util.Clock
import AuthTokenCleaner.Clean
import net.channull.models.services.AuthTokenService
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A job which cleanup invalid auth tokens.
 *
 * @param service The auth token service implementation.
 * @param clock The clock implementation.
 */
class AuthTokenCleaner @Inject() (
  service: AuthTokenService,
  clock: Clock)
  extends Actor with Logging {

  /**
   * Process the received messages.
   */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.getMillis
      service.clean.map { deleted =>
        val seconds = (clock.now.getMillis - start) / 1000
        logger.info("Total of %s auth tokens(s) were deleted in %s seconds".format(deleted.length, seconds))
      }.recover {
        case e =>
          logger.error("Couldn't cleanup auth tokens because of unexpected error", e)
      }
  }
}

/**
 * The companion object.
 */
object AuthTokenCleaner {
  case object Clean
}
