package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullBan(id: UUID, chanNull: ChanNull, user: User, bannedBy: User, reason: String, whenCreated: Instant,
  expiry: Instant, violatedRules: Seq[ChanNullRule])
