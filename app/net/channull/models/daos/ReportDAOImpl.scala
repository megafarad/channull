package net.channull.models.daos

import net.channull.models._
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends ReportDAO with DAOSlick {

  private sealed trait ReportQuery
  private case class ById(id: UUID) extends ReportQuery
  private case class ByChanNullId(chanNullId: UUID, page: Int, pageSize: Int) extends ReportQuery
  private def reportRowsQuery(query: ReportQuery): Query[(ReportTable, UserTable), (ReportRow, User), Seq] = {
    val filteredQuery = query match {
      case ById(id) => reportTableQuery.filter(_.id === id)
      case ByChanNullId(chanNullId, page, pageSize) => chanNullPostTableQuery.filter(_.chanNullId === chanNullId)
        .join(reportTableQuery).on(_.id === _.postId)
        .map(_._2)
        .drop(page * pageSize)
        .take(pageSize)
    }
    filteredQuery.join(userTableQuery).on(_.reporter === _.id)
  }

  private def reportsWithViolatedRules(query: ReportQuery) = {
    val rulesQuery = reportViolatedRuleTableQuery.join(chanNullRuleTableQuery).on(_.violatedRuleId === _.id)
      .join(userTableQuery).on(_._2.whoCreated === _.id)

    val baseQuery = reportRowsQuery(query)
      .joinLeft(rulesQuery).on(_._1.id === _._1._1.reportId)

    baseQuery.result.map { rows =>
      rows.groupBy {
        case ((reportFields, reporterUser), _) => (reportFields, reporterUser)
      }.view.mapValues(values => values.flatMap {
        case (_, Some(violatedRuleRow)) => Seq(violatedRuleRow)
        case (_, None) => Nil
      })
    }
  }

  private def getReports(query: ReportQuery) = db.run(reportsWithViolatedRules(query)).map {
    view => view.toSeq.map {
      case ((reportRow, reporterUser), violatedRulesRows) =>
        val violatedRules = violatedRulesRows.map {
          case ((_,ruleRow), user) => ChanNullRule(ruleRow.id, ruleRow.chanNullID, ruleRow.number, ruleRow.rule,
            ruleRow.whenCreated, user)
        }
        Report(reportRow.id, reporterUser, reportRow.postId, reportRow.report, violatedRules, reportRow.timestamp,
          reportRow.status)
    }
  }

  private def totalReportsQuery(chanNullId: UUID) = chanNullPostTableQuery.filter(_.chanNullId === chanNullId)
    .join(reportTableQuery).on(_.id === _.postId)
    .map(_._2)

  def getByChanNullId(chanNullId: UUID, page: Int, pageSize: Int): Future[Page[Report]] = for {
    reports <- getReports(ByChanNullId(chanNullId, page, pageSize))
    totalCount <- db.run(totalReportsQuery(chanNullId).length.result)
  } yield Page(reports, page, page * pageSize, totalCount.toLong)

  def upsert(request: UpsertReportRequest): Future[Report] = {
    val upsertRowAction = reportTableQuery.insertOrUpdate(ReportRow(
      request.id, request.reporterId, request.postId, request.report, request.timestamp, request.status
    ))
    val upsertViolatedRulesActions = request.violatedRules map {
      upsertRequest => reportViolatedRuleTableQuery.insertOrUpdate(ReportViolatedRuleRow(
        upsertRequest.id, request.id, upsertRequest.violatedRuleId
      ))
    }
    db.run(DBIO.sequence(upsertRowAction +: upsertViolatedRulesActions).transactionally).flatMap {
      _ => getReports(ById(request.id)).map(_.head)
    }
  }
}
