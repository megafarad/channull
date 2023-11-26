package net.channull.models

import java.time.Instant
import java.util.UUID

case class UpsertChanNullBanRequest(id: UUID, chanNullId: UUID, userId: UUID, bannedByUserId: UUID,
  reason: Option[String], whenCreated: Instant, expiry: Option[Instant],
  violatedRules: Seq[UpsertChanNullBanViolatedRuleRequest])

case class UpsertChanNullBanViolatedRuleRequest(id: UUID, violatedRuleId: UUID)
