package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullRule(id: UUID, number: Short, rule: String, whenCreated: Instant, whoCreated: User)
