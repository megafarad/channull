package net.channull.models.daos

import net.channull.models.ChanNullPermissions

import java.util.UUID
import scala.concurrent.Future

trait ChanNullPermissionsDAO {

  /**
   * Gets permissions for a ChanNull
   * @param chanNullId  The ID of the ChanNull
   * @return
   */
  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullPermissions]]

  /**
   * Upserts ChanNull Permissions
   * @param permissions The permissions to upsert
   * @return
   */
  def upsert(permissions: ChanNullPermissions): Future[ChanNullPermissions]
}
