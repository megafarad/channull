package net.channull.models

import java.time.Instant
import java.util.UUID

case class UpsertReportRequest(id: UUID, reporterId: UUID, postId: UUID, report: String,
                               violatedRules: Seq[UpsertReportViolatedRuleRequest], timestamp: Instant,
                               status: ReportStatus.Value)

case class UpsertReportViolatedRuleRequest(id: UUID, violatedRuleId: UUID)
