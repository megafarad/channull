package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPostDAO {

  def getPosts(chanNullName: String, page: Int, pageSize: Int): Future[Page[ChanNullPost]]
  def getPost(postId: UUID): Future[Option[ChanNullPost]]
  def save(post: ChanNullPost): Future[ChanNullPost]


}
