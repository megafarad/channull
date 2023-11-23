package net.channull.models.daos

import net.channull.models.ChanNullPermissions
import play.api.db.slick.DatabaseConfigProvider
import PostgresProfile.api._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChanNullPermissionsDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: ExecutionContext) extends ChanNullPermissionsDAO with DAOSlick {

  private def getByChanNullIdQuery(chanNullId: UUID) = chanNullPermissionsTableQuery.filter(_.chanNullId === chanNullId)

  private def upsertAction(permissions: ChanNullPermissions) = chanNullPermissionsTableQuery.insertOrUpdate(permissions)

  def getByChanNullId(chanNullId: UUID): Future[Seq[ChanNullPermissions]] = db.run(getByChanNullIdQuery(chanNullId)
    .result)

  def upsert(permissions: ChanNullPermissions): Future[ChanNullPermissions] = db.run(upsertAction(permissions))
    .map(_ => permissions)
}
