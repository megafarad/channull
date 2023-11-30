package net.channull.models

import java.util.UUID

case class UpsertChanNullUserRequest(id: UUID, chanNullId: UUID, userId: UUID, role: UserRole.Value)
