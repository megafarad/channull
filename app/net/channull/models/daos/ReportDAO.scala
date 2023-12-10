package net.channull.models.daos

import net.channull.models._

import java.util.UUID
import scala.concurrent.Future

trait ReportDAO {

  /**
   * Retrieves reports by ChanNull ID.
   *
   * @param chanNullId The ChanNull ID to filter the reports.
   * @param page       The page number to retrieve.
   * @param pageSize   The number of reports per page.
   * @return A Future containing a Page of Report objects that match the given ChanNull ID.
   */
  def getByChanNullId(chanNullId: UUID, page: Int, pageSize: Int): Future[Page[Report]]

  /**
   * Upserts a report based on the given request.
   *
   * @param request the UpsertReportRequest object representing the report to upsert
   * @return a Future of Report representing the upserted report
   */
  def upsert(request: UpsertReportRequest): Future[Report]

}
