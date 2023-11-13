package net.channull.models

import java.util.UUID

case class ChanNullPermissions(id: UUID, chanNull: ChanNull, role: UserRole.Value, canPost: Boolean,
  canSubPost: Boolean, canBan: Boolean)
