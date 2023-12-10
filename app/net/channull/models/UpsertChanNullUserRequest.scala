package net.channull.models

import java.util.UUID

case class UpsertChanNullUserRequest(chanNullId: UUID, userId: UUID, role: UserRole.Value, lastReadMessageId: Option[UUID])
