package net.channull.models.daos

import net.channull.models.{ChanNullAccess, _}
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.collection.View
import scala.concurrent.{ExecutionContext, Future}

class ChanNullDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ChanNullDAO with DAOSlick {

  private sealed trait ChanNullQuery
  private case class ByID(id: UUID) extends ChanNullQuery
  private case class ByName(name: String) extends ChanNullQuery
  private case object RandomPublic extends ChanNullQuery

  protected def chanNullRowQuery(query: ChanNullQuery): Query[(Rep[UUID], Rep[Option[UUID]], Rep[String], UserTable, Rep[String], Rep[Instant], Rep[ChanNullAccess.Value]), (UUID, Option[UUID], String, User, String, Instant, ChanNullAccess.Value), Seq] = {
    val filteredQuery = query match {
      case ByID(id) => chanNullTableQuery.filter(_.id === id)
      case ByName(name) => chanNullTableQuery.filter(_.name === name)
      case RandomPublic => randomPublicChanNullQuery
    }
    for {
      (chanNull, createdByUser) <- filteredQuery join userTableQuery on (_.whoCreated === _.id)
    } yield (chanNull.id, chanNull.parentId, chanNull.name, createdByUser, chanNull.description, chanNull.whenCreated,
      chanNull.access)
  }

  protected def chanNullRowWithRules(query: ChanNullQuery): DBIOAction[View[(UUID, Option[UUID], String, User, String, Instant, ChanNullAccess.Value, Seq[ChanNullRule])], NoStream, Effect.Read] = chanNullRowQuery(query)
    .join(chanNullRuleTableQuery).on(_._1 === _.chanNullId)
    .join(userTableQuery).on(_._2.whoCreated === _.id)
    .result.map { rows =>
      rows.groupBy {
        case ((chanNullFields, _), _) => chanNullFields
      }.view.mapValues(values => values.map { case ((_, rules), rulesCreatedBy) => (rules, rulesCreatedBy)})
    }.map {
      result =>
        result map {
          case ((id, parentId, name, createdBy, description, whenCreated, access), rulesFields) =>
            val chanNullRules = rulesFields.map {
              case (rule, createdByUser) => ChanNullRule(rule.id, rule.chanNullID, rule.number,
                rule.rule, rule.whenCreated, createdByUser)
            }
            (id, parentId, name, createdBy, description, whenCreated, access, chanNullRules)
        }
    }

  protected def getChanNullRecursive(query: ChanNullQuery): Future[Option[ChanNull]] = db.run(chanNullRowWithRules(query))
    .flatMap {
      fields =>
        fields.headOption match {
          case Some((id, maybeParentId, name, createdBy, description, whenCreated, access, chanNullRules)) =>
            maybeParentId match {
              case Some(parentId) => getChanNullRecursive(ByID(parentId)) map {
                parentChanNull => Some(ChanNull(id, parentChanNull, name, createdBy, description, chanNullRules,
                  whenCreated, access))
              }
              case None => Future.successful(Some(ChanNull(id, None, name, createdBy, description, chanNullRules,
                whenCreated, access)))
            }
          case None => Future.successful(None)
        }
    }

  def get(id: UUID): Future[Option[ChanNull]] = getChanNullRecursive(ByID(id))

  def get(name: String): Future[Option[ChanNull]] = getChanNullRecursive(ByName(name))

  def getRandomPublic: Future[Option[ChanNull]] = getChanNullRecursive(RandomPublic)

  def upsert(request: UpsertChanNullRequest): Future[ChanNull] = {
    val upsertRowAction = chanNullTableQuery.insertOrUpdate(ChanNullRow(
      id = request.id, parentId = request.parentId, name = request.name, description = request.description,
      whenCreated = request.whenCreated, whoCreated = request.creator, access = request.access
    ))

    val upsertRulesActions = request.rulesUpsertRequests.map {
      upsertRequest => chanNullRuleTableQuery.insertOrUpdate(ChanNullRuleRow(upsertRequest.id, upsertRequest.chanNullId,
        upsertRequest.number,upsertRequest.rule, upsertRequest.whenCreated, upsertRequest.whoCreated))
    }
    db.run(DBIO.sequence(upsertRowAction +: upsertRulesActions).transactionally).flatMap {
      _ => get(request.id).map(_.get) //Should be a safe get. We just inserted the record...
    }
  }
}
