package net.channull.models.daos

import net.channull.models._
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ChanNullDAO with DAOSlick {

  private sealed trait ChanNullQuery
  private case class ByID(id: UUID) extends ChanNullQuery
  private case class ByParentId(parentId: UUID) extends ChanNullQuery
  private case class ByName(name: String) extends ChanNullQuery
  private case object RandomPublic extends ChanNullQuery

  private def chanNullRowQuery(query: ChanNullQuery) = {
    val filteredQuery = query match {
      case ByID(id) => chanNullTableQuery.filter(_.id === id)
      case ByName(name) => chanNullTableQuery.filter(_.name === name)
      case ByParentId(parentId) => chanNullTableQuery.filter(_.parentId === parentId)
      case RandomPublic => randomPublicChanNullQuery
    }
    for {
      (chanNull, createdByUser) <- filteredQuery join userTableQuery on (_.whoCreated === _.id)
    } yield (chanNull.id, chanNull.parentId, chanNull.name, createdByUser, chanNull.description, chanNull.whenCreated,
      chanNull.access)
  }

  private def chanNullRowWithRules(query: ChanNullQuery) = {

    val rulesQuery = chanNullRuleTableQuery.join(userTableQuery).on(_.whoCreated === _.id)

    val baseQuery = chanNullRowQuery(query)
      .joinLeft(rulesQuery).on(_._1 === _._1.chanNullId)

    baseQuery.result.map { rows =>
      rows.groupBy {
        case (chanNullFields, _) => chanNullFields
      }.view.mapValues(values => values.flatMap {
        case (_, Some((rules, rulesCreatedBy))) => Seq((rules, rulesCreatedBy))
        case (_, None) => Nil
      })
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

  }
  private def getChanNullRecursive(query: ChanNullQuery): Future[Seq[ChanNull]] = db.run(chanNullRowWithRules(query))
    .flatMap {
      view =>
        Future.sequence(view.toSeq.map {
          case (id, _, name, createdBy, description, whenCreated, access, chanNullRules) =>
            getChanNullRecursive(ByParentId(id)) map {
              children => ChanNull(id, name, createdBy, description, chanNullRules, whenCreated, access, children)
            }
        })

    }

  def get(id: UUID): Future[Option[ChanNull]] = getChanNullRecursive(ByID(id)).map(_.headOption)

  def get(name: String): Future[Option[ChanNull]] = getChanNullRecursive(ByName(name)).map(_.headOption)

  def getRandomPublic: Future[Option[ChanNull]] = getChanNullRecursive(RandomPublic).map(_.headOption)

  def upsert(request: UpsertChanNullRequest): Future[ChanNull] = {
    val upsertRowAction = chanNullTableQuery.insertOrUpdate(ChanNullRow(
      id = request.id, parentId = request.parentId, name = request.name, description = request.description,
      whenCreated = request.whenCreated, whoCreated = request.creator, access = request.access
    ))

    val upsertRulesActions = request.rulesUpsertRequests.map {
      upsertRequest =>
        chanNullRuleTableQuery.insertOrUpdate(ChanNullRuleRow(upsertRequest.id, upsertRequest.chanNullId,
          upsertRequest.number, upsertRequest.rule, upsertRequest.whenCreated, upsertRequest.whoCreated))
    }
    db.run(DBIO.sequence(upsertRowAction +: upsertRulesActions).transactionally).flatMap {
      _ => get(request.id).map(_.get) //Should be a safe get. We just inserted the record...
    }
  }
}
