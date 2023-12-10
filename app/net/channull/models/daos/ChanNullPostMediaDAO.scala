package net.channull.models.daos

import net.channull.models.ChanNullPostMedia

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPostMediaDAO {

  /**
   * Gets media for a post
   * @param postId  The ID of the post
   * @return
   */
  def getMediaForPost(postId: UUID): Future[Seq[ChanNullPostMedia]]

  /**
   * Gets a specific post media
   * @param id  The ID of the post media
   * @return
   */
  def get(id: UUID): Future[Option[ChanNullPostMedia]]

  /**
   * Upserts a post media
   * @param media The media to upsert
   * @return
   */
  def upsert(media: ChanNullPostMedia): Future[ChanNullPostMedia]

  /**
   * Deletes a post media
   * @param id  The ID of the post media to delete
   * @return
   */
  def delete(id: UUID): Future[Unit]
}
