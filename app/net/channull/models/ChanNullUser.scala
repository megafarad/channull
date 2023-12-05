package net.channull.models

import java.util.UUID

case class ChanNullUser(id: UUID, chanNullId: UUID, user: User, role: UserRole.Value, lastReadMessageId: Option[UUID])
