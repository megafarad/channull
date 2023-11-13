package net.channull.models

object ChanNullAccess extends Enumeration {
  type ChanNullAccess = Value

  val Public: ChanNullAccess.Value = Value("Public")
  val Private: ChanNullAccess.Value = Value("Private")
}
