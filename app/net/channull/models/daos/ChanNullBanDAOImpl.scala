package net.channull.models.daos

import net.channull.models._
import PostgresProfile.api._
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ChanNullBanDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends ChanNullBanDAO with DAOSlick with Logging {

  private sealed trait ChanNullBanQuery
  private case class ById(id: UUID) extends ChanNullBanQuery
  private case class ByChanNullId(chanNullId: UUID) extends ChanNullBanQuery
  private def banRowsQuery(query: ChanNullBanQuery) = {
    val filteredQuery = query match {
      case ById(id) => chanNullBanTableQuery.filter(tbl => tbl.id === id && (tbl.expiry.isEmpty || tbl.expiry >=
        Instant.now()))
      case ByChanNullId(chanNullId) => chanNullBanTableQuery.filter(tbl => tbl.chanNullId === chanNullId &&
        (tbl.expiry.isEmpty || tbl.expiry >= Instant.now()))
    }

    filteredQuery.join(userTableQuery).on(_.userId === _.id).join(userTableQuery).on(_._1.bannedBy === _.id)
      .map {
        case ((banTable, bannedUser), bannedByUser) =>
          (banTable.id, banTable.chanNullId, bannedUser, bannedByUser, banTable.reason, banTable.whenCreated,
            banTable.expiry)
      }
  }

  private def banRowsWithViolatedRules(query: ChanNullBanQuery) = {

    val rulesQuery = chanNullBanViolatedRuleTableQuery.join(chanNullRuleTableQuery).on(_.violatedRuleId === _.id)
      .join(userTableQuery).on(_._2.whoCreated === _.id)

    val baseQuery = banRowsQuery(query)
      .joinLeft(rulesQuery).on(_._1 === _._1._1.banId)
    baseQuery.result.map { rows =>
      rows.groupBy {
        case (banFields, _) => banFields
      }.view.mapValues(values => values.flatMap {
        case (_, Some(violatedRuleRow)) => Seq(violatedRuleRow)
        case (_, None) => Nil
      })
    }
  }

  private def getBans(query: ChanNullBanQuery): Future[Seq[ChanNullBan]] = db.run(banRowsWithViolatedRules(query)).map {
    view =>
      view.toSeq.map {
        case ((banId, banChanNullId, bannedUser, bannedByUser, reason, whenCreated, expiry), violatedRulesRows) =>
          val violatedRules = violatedRulesRows.map {
            case ((_, ruleRow), createdBy) =>
              ChanNullRule(id = ruleRow.id, chanNullId = ruleRow.chanNullID, number = ruleRow.number, rule = ruleRow.rule,
                whenCreated = ruleRow.whenCreated, whoCreated = createdBy)
          }
          ChanNullBan(
            id = banId, chanNullId = banChanNullId, user = bannedUser, bannedBy = bannedByUser, reason = reason,
            whenCreated = whenCreated, expiry = expiry, violatedRules = violatedRules)
      }
  }

  /**
   *
   * Upserts a ChanNull Ban
   *
   * @param request   The request to upsert
   * @return
   */
  def upsert(request: UpsertChanNullBanRequest): Future[ChanNullBan] = {
    val upsertRowAction = chanNullBanTableQuery.insertOrUpdate(ChanNullBanRow(
      id = request.id, chanNullId = request.chanNullId, userId = request.userId, bannedBy = request.bannedByUserId,
      reason = request.reason, whenCreated = request.whenCreated, expiry = request.expiry))
    val upsertViolatedRulesActions = request.violatedRules map {
      upsertRequest =>
        chanNullBanViolatedRuleTableQuery.insertOrUpdate(ChanNullBanViolatedRuleRow(
          id = upsertRequest.id, banId = request.id, violatedRuleId = upsertRequest.violatedRuleId
        ))
    }
    db.run(DBIO.sequence(upsertRowAction +: upsertViolatedRulesActions).transactionally).flatMap {
      _ => getBans(ById(request.id)).map(_.head)
    }

  }

  /**
   *
   * Deletes a ChanNull Ban
   *
   * @param id The ID of the ban
   * @return
   */
  def delete(id: UUID): Future[Unit] = db.run(chanNullBanTableQuery.filter(_.id === id).delete).map(_ => ())

  /**
   *
   * Gets bans by ChanNull ID
   *
   * @param chanNullId  The ID of the ChanNull
   * @return
   */
  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullBan]] = getBans(ByChanNullId(chanNullId))

  /**
   * Gets IDs of expired bans
   *
   * @param dateTime The current date/time
   * @return A Seq of ban IDs
   */
  def findExpired(dateTime: Instant): Future[Seq[UUID]] = db.run(chanNullBanTableQuery.filter(tbl =>
    tbl.expiry.isDefined && tbl.expiry < dateTime).map(_.id).result)
}
