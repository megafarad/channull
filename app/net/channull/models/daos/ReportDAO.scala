package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ReportDAO {

  def getByChanNullId(chanNullId: UUID, page: Int, pageSize: Int): Future[Page[Report]]

  def upsert(request: UpsertReportRequest): Future[Report]


}
