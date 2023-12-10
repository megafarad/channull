package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPostDAO {

  /**
   * Get paginated posts for a ChanNull
   *
   * @param chanNullName    The name of the ChanNull
   * @param loggedInUserId  The ID of the user, if logged in
   * @param page            The page of the output
   * @param pageSize        The page size in the output
   * @return
   */
  def getPosts(chanNullName: String, loggedInUserId: Option[UUID], page: Int, pageSize: Int): Future[Page[ChanNullPost]]

  /**
   * Gets a specific post
   *
   * @param postId          The ID of the post to get
   * @param loggedInUserId  The ID of the user, if logged in
   * @return
   */
  def getPost(postId: UUID, loggedInUserId: Option[UUID]): Future[Option[ChanNullPost]]

  /**
   * Upserts a ChanNull Post
   *
   * @param upsertRequest   The request to upsert
   * @return
   */
  def upsert(upsertRequest: UpsertChanNullPostRequest): Future[ChanNullPost]

  /**
   * Deletes the post. Descendant posts, related reactions and media also get deleted with cascading deletes.
   *
   * @param postId The ID of the post
   * @return
   */
  def delete(postId: UUID): Future[Unit]

}
