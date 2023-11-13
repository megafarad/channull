package net.channull.models

import java.time.Instant
import java.util.UUID

case class Report(id: UUID, reporter: User, post: ChanNullPost, report: String, violatedRules: Seq[ChanNullRule],
                  timestamp: Instant, status: ReportStatus.Value)
