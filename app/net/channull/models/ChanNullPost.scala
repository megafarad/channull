package net.channull.models

import java.time.Instant
import java.util.UUID

case class ChanNullPost(id: UUID, chanNullId: UUID, text: Option[String], reactions: Seq[ChanNullPostReaction],
                        whenCreated: Instant, whoCreated: User, expiry: Option[Instant], children: Seq[ChanNullPost])
