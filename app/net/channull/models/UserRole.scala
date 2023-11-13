package net.channull.models

import net.channull.models

object UserRole extends Enumeration {
  type UserRole = Value

  val User: models.UserRole.Value = Value("user")
  val Moderator: models.UserRole.Value = Value("moderator")
  val Admin: models.UserRole.Value = Value("admin")
}
