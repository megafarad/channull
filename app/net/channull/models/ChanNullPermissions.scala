package net.channull.models

import java.util.UUID

case class ChanNullPermissions(id: UUID, chanNullId: UUID, role: UserRole.Value, canPost: Boolean, canSubPost: Boolean,
  canBan: Boolean)
