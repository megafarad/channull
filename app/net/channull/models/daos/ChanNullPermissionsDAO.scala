package net.channull.models.daos

import net.channull.models.ChanNullPermissions

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPermissionsDAO {
  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullPermissions]]
  def upsert(permissions: ChanNullPermissions): Future[ChanNullPermissions]
}
