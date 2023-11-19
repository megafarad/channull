package net.channull.models

import java.time.Instant
import java.util.UUID

case class UpsertChanNullPostRequest(id: UUID, parentId: Option[UUID], chanNullId: UUID, text: Option[String],
                                     whenCreated: Instant, whoCreated: UUID, expiry: Option[Instant])
