package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNull(id: UUID, parent: Option[ChanNull], name: String, creator: User, description: String,
                    rules: Seq[ChanNullRule], whenCreated: Instant, access: ChanNullAccess.Value)