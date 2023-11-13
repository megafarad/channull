package net.channull.models

import java.time.Instant
import java.util.UUID

case class BlockedUser(id: UUID, blockingUser: User, blockedUser: User, timestamp: Instant)
