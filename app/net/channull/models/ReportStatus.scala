package net.channull.models

object ReportStatus extends Enumeration {
  type ReportStatus = Value
  val Pending: ReportStatus.Value = Value("Pending")
  val UnderReview: ReportStatus.Value = Value("Under_Review")
  val Closed: ReportStatus.Value = Value("Closed")
}
