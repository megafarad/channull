package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullRule(id: UUID, chanNullId: UUID, number: Short, rule: String, whenCreated: Instant, whoCreated: User)
