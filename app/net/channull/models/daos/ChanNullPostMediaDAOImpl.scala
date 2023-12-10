package net.channull.models.daos

import net.channull.models._
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._
import play.api.Logging

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullPostMediaDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ChanNullPostMediaDAO with DAOSlick
  with Logging {
  private def queryById(id: UUID) = chanNullPostMediaTableQuery.filter(_.id === id)

  private def queryByPostId(postId: UUID) = chanNullPostMediaTableQuery.filter(_.postId === postId)

  private def upsertAction(media: ChanNullPostMedia) = chanNullPostMediaTableQuery.insertOrUpdate(media)

  /**
   * Gets media for a post
   * @param postId  The ID of the post
   * @return
   */
  def getMediaForPost(postId: UUID): Future[Seq[ChanNullPostMedia]] = db.run(queryByPostId(postId).result)

  /**
   * Gets a specific post media
   * @param id  The ID of the post media
   * @return
   */
  def get(id: UUID): Future[Option[ChanNullPostMedia]] = db.run(queryById(id).result.headOption)

  /**
   * Upserts a post media
   * @param media The media to upsert
   * @return
   */
  def upsert(media: ChanNullPostMedia): Future[ChanNullPostMedia] = db.run(upsertAction(media)).map(_ => media)

  /**
   * Deletes a post media
   * @param id  The ID of the post media to delete
   * @return
   */
  def delete(id: UUID): Future[Unit] = db.run(queryById(id).delete).map(_ => ())
}
