package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullPost(id: UUID, parent: Option[ChanNullPost], chanNull: ChanNull, text: Option[String],
  reactions: Seq[ChanNullPostReaction], whenCreated: Instant, whoCreated: User, expiry: Instant)
