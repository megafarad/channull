package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPostDAO {

  def getPosts(chanNullName: String, loggedInUserId: Option[UUID], page: Int, pageSize: Int): Future[Page[ChanNullPost]]

  def getPost(postId: UUID, loggedInUserId: Option[UUID]): Future[Option[ChanNullPost]]

  def upsert(upsertRequest: UpsertChanNullPostRequest): Future[ChanNullPost]

  def delete(postId: UUID): Future[Unit]

}
