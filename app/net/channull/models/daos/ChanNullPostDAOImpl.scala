package net.channull.models.daos

import net.channull.models._
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._
import play.api.Logging

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullPostDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ChanNullPostDAO with DAOSlick with Logging {

  private sealed trait ChanNullPostQuery {
    val loggedInUserId: Option[UUID]
  }
  private case class ByID(id: UUID, loggedInUserId: Option[UUID]) extends ChanNullPostQuery
  private case class ByParentID(id: UUID, loggedInUserId: Option[UUID]) extends ChanNullPostQuery
  private case class NullParent(chanNullName: String, loggedInUserId: Option[UUID], page: Int, pageSize: Int)
    extends ChanNullPostQuery



  private def postRowQuery(postQuery: ChanNullPostQuery) = {
    val filteredQuery: Query[ChanNullPostTable, ChanNullPostRow, Seq] = postQuery match {
      case ByID(id, loggedInUserId) =>
        val baseQuery = chanNullPostTableQuery.filter(_.id === id)
        loggedInUserId match {
          case Some(userId) => baseQuery.filterNot(isBlocked(userId, _))
          case None => baseQuery
        }
      case ByParentID(parentID, loggedInUserId) =>
        val baseQuery = chanNullPostTableQuery.filter(_.parentId === parentID)
        loggedInUserId match {
          case Some(userId) => baseQuery.filterNot(isBlocked(userId, _))
          case None => baseQuery
        }
      case NullParent(chanNullName, loggedInUserId, page, pageSize) =>
        val offset = page * pageSize
        val baseQuery = chanNullTableQuery.filter(_.name === chanNullName).join(chanNullPostTableQuery)
          .on(_.id === _.chanNullId).filter(_._2.parentId.isEmpty).drop(offset).take(pageSize)
          .map(_._2)
        loggedInUserId match {
          case Some(userId) => baseQuery.filterNot(isBlocked(userId, _))
          case None => baseQuery
        }
    }

    filteredQuery.join(userTableQuery).on(_.whoCreated === _.id)
      .map {
        case (postTable, userTable) =>
          (postTable.id, postTable.parentId, postTable.chanNullId, postTable.text, postTable.whenCreated, userTable,
            postTable.expiry)
      }
  }

  private def isBlocked(retrievingUserId: UUID, post: ChanNullPostTable) = {
    post.whoCreated in blockedUserTableQuery.filter(blockedUser => blockedUser.blockedUserId ===
      retrievingUserId).map(_.blockingUserId)
  }

  private def totalChanNullPostsQuery(chanNullName: String, loggedInUserId: Option[UUID]) = {
    val baseQuery = chanNullTableQuery.filter(_.name === chanNullName)
      .join(chanNullPostTableQuery).on(_.id === _.chanNullId).map(_._2)
    loggedInUserId match {
      case Some(userId) => baseQuery.filterNot(isBlocked(userId, _))
      case None => baseQuery
    }
  }

  private def postRowWithReactions(postQuery: ChanNullPostQuery) = {

    val reactionsQuery = chanNullPostReactionTableQuery.join(userTableQuery).on(_.userId === _.id)

    val baseQuery = postRowQuery(postQuery)
      .joinLeft(reactionsQuery).on(_._1 === _._1.postId).sortBy(_._1._5)

    baseQuery.result.map { rows =>
      rows.groupBy {
        case (postFields, _) => postFields
      }.view.mapValues(values => values.flatMap {
        case (_, Some((reaction, reactBy))) => Seq((reaction, reactBy))
        case (_, None) => Nil
      })
    }.map {
      result =>
        result map {
          case ((postId, parentPostId, chanNullId, postText, whenCreated, createdBy, expiry), reactionFields) =>
            val reactions = reactionFields.map {
              case (reaction, reactionCreatedBy) => ChanNullPostReaction(reaction.id, reaction.postId,
                reactionCreatedBy, reaction.reactionType)
            }
            (postId, parentPostId, chanNullId, postText, whenCreated, createdBy, expiry, reactions)
        }
    }
  }

  private def getPostsRecursive(query: ChanNullPostQuery): Future[Seq[ChanNullPost]] = db.run(postRowWithReactions(query)).flatMap {
    view =>
      Future.sequence(view.toSeq.map {
        case (postId, _, chanNullId, postText, whenCreated, createdBy, expiry, reactions) =>
          getPostsRecursive(ByParentID(postId, query.loggedInUserId)) map {
            children => ChanNullPost(postId, chanNullId, postText, reactions, whenCreated, createdBy, expiry, children)
          }
      })
  }

  private def deleteAction(id: UUID) = chanNullPostTableQuery.filter(_.id === id).delete

  private def addAction(upsertRequest: UpsertChanNullPostRequest) =
    chanNullPostTableQuery.insertOrUpdate(ChanNullPostRow(id = upsertRequest.id, parentId = upsertRequest.parentId,
      chanNullId = upsertRequest.chanNullId, text = upsertRequest.text, whenCreated = upsertRequest.whenCreated,
      whoCreated = upsertRequest.whoCreated, expiry = upsertRequest.expiry))

  /**
   * Get paginated posts for a ChanNull
   *
   * @param chanNullName    The name of the ChanNull
   * @param loggedInUserId  The ID of the user, if logged in
   * @param page            The page of the output
   * @param pageSize        The page size in the output
   * @return
   */
  def getPosts(chanNullName: String, loggedInUserId: Option[UUID], page: Int, pageSize: Int): Future[Page[ChanNullPost]] = for {
    posts <- getPostsRecursive(NullParent(chanNullName, loggedInUserId, page, pageSize))
    totalCount <- db.run(totalChanNullPostsQuery(chanNullName, loggedInUserId).length.result)
  } yield Page(posts, page, page * pageSize, totalCount.toLong)

  /**
   * Gets a specific post
   *
   * @param postId          The ID of the post to get
   * @param loggedInUserId  The ID of the user, if logged in
   * @return
   */
  def getPost(postId: UUID, loggedInUserId: Option[UUID]): Future[Option[ChanNullPost]] = getPostsRecursive(ByID(postId,
    loggedInUserId)).map(_.headOption)

  /**
   * Upserts a ChanNull Post
   *
   * @param upsertRequest   The request to upsert
   * @return
   */
  def upsert(upsertRequest: UpsertChanNullPostRequest): Future[ChanNullPost] = db.run(addAction(upsertRequest))
    .flatMap {
      _ =>
        getPost(upsertRequest.id, None).map(_.get)
    }

  /**
   * Deletes the post. Descendant posts, related reactions and media also get deleted with cascading deletes.
   *
   * @param postId The ID of the post
   * @return
   */
  def delete(postId: UUID): Future[Unit] = db.run(deleteAction(postId)).map(_ => ())
}
