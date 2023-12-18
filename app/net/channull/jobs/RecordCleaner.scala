package net.channull.jobs

import javax.inject.Inject
import akka.actor._
import io.github.honeycombcheesecake.play.silhouette.api.util.Clock
import RecordCleaner.Clean
import net.channull.models.services.{ AuthTokenService, ChanNullService }
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A job which cleans up expired records.
 *
 * @param authTokenService The auth token service implementation.
 * @param clock The clock implementation.
 */
class RecordCleaner @Inject() (authTokenService: AuthTokenService, chanNullService: ChanNullService,
  clock: Clock)
  extends Actor with Logging {

  /**
   * Process the received messages.
   */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.getMillis
      (for {
        deletedTokens <- authTokenService.clean
        deletedBanIds <- chanNullService.cleanBans
        deletedPostIds <- chanNullService.cleanPosts
      } yield {
        val seconds = (clock.now.getMillis - start) / 1000
        logger.info("Total of %s auth tokens(s), %s ban(s), and %s post(s) were deleted in %s seconds".format(
          deletedTokens.length, deletedBanIds.length, deletedPostIds.length, seconds))
      }).recover {
        case e =>
          logger.error("Couldn't cleanup auth tokens because of unexpected error", e)
      }
  }
}

/**
 * The companion object.
 */
object RecordCleaner {
  case object Clean
}
