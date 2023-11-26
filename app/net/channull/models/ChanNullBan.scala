package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullBan(id: UUID, chanNullId: UUID, user: User, bannedBy: User, reason: Option[String],
  whenCreated: Instant, expiry: Option[Instant], violatedRules: Seq[ChanNullRule])
