package net.channull.models

import java.time.Instant
import java.util.UUID

case class UpsertBlockedUserRequest(id: UUID, blockingUserId: UUID, blockedUserId: UUID, timestamp: Instant)
