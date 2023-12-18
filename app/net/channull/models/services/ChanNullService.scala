package net.channull.models.services

import java.util.UUID
import scala.concurrent.Future

trait ChanNullService {

  /**
   * Deletes expired posts
   * @return IDs of deleted posts
   */
  def cleanPosts: Future[Seq[UUID]]

  /**
   * Deletes expired bans
   * @return IDs of deleted bans
   */
  def cleanBans: Future[Seq[UUID]]
}
