package net.channull.models.daos

import net.channull.models.ChanNullPostMedia

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPostMediaDAO {

  def getMediaForPost(postId: UUID): Future[Seq[ChanNullPostMedia]]

  def get(id: UUID): Future[Option[ChanNullPostMedia]]

  def save(media: ChanNullPostMedia): Future[ChanNullPostMedia]

  def delete(media: ChanNullPostMedia): Future[Unit]
}
