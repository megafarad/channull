package net.channull.models

import java.util.UUID

case class ChanNullUser(chanNullId: UUID, user: User, role: UserRole.Value, lastReadMessageId: Option[UUID])
