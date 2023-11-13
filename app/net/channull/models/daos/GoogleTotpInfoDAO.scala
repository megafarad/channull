package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.GoogleTotpInfo
import io.github.honeycombcheesecake.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

class GoogleTotpInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[GoogleTotpInfo] with DAOSlick {

  val classTag: ClassTag[GoogleTotpInfo] = scala.reflect.classTag[GoogleTotpInfo]

  import profile.api._

  protected def googleTotpInfoQuery(loginInfo: LoginInfo) = for {
    loginInfoTable <- loginInfoQuery(loginInfo)
    googleTotpInfoTable <- googleTotpInfoTableQuery if loginInfoTable.id === googleTotpInfoTable.loginInfoID
  } yield googleTotpInfoTable

  // Use subquery workaround instead of join to get authinfo because slick only supports selecting
  // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
  protected def googleTotpInfoSubQuery(loginInfo: LoginInfo) =
    googleTotpInfoTableQuery.filter(_.loginInfoID in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: GoogleTotpInfo) = {
    (for {
      loginInfoRow <- loginInfoQuery(loginInfo).result.head
      totpGoogleId <- (googleTotpInfoTableQuery returning googleTotpInfoTableQuery.map(_.id))
        .insertOrUpdate(GoogleTotpInfoRow(None, loginInfoRow.id.get, authInfo.sharedKey)).map(_.head)
      scratchCodeRows = authInfo.scratchCodes.map(code => TotpScratchCodeRow(None, totpGoogleId, code.hasher,
        code.password, code.salt))
      _ <- totpScratchCodeTableQuery ++= scratchCodeRows
    } yield authInfo).transactionally
  }

  protected def updateAction(loginInfo: LoginInfo, authInfo: GoogleTotpInfo) = {
    (for {
      totpGoogleTableRow <- googleTotpInfoSubQuery(loginInfo).result.head
      _ <- (for { sc <- totpScratchCodeTableQuery if sc.totpGoogleInfoId === totpGoogleTableRow.id.get } yield sc).delete
      _ <- totpScratchCodeTableQuery ++= authInfo.scratchCodes.map(scratchCode => TotpScratchCodeRow(
        None,
        totpGoogleTableRow.id.get, scratchCode.hasher, scratchCode.password, scratchCode.salt))
      _ <- googleTotpInfoTableQuery.filter(_.id === totpGoogleTableRow.id.get).map(_.sharedKey).update(authInfo.sharedKey)
    } yield authInfo).transactionally
  }

  def find(loginInfo: LoginInfo): Future[Option[GoogleTotpInfo]] = db.run {
    googleTotpInfoQuery(loginInfo).join(totpScratchCodeTableQuery)
      .result.map { rows =>
        rows.groupBy {
          case (totpGoogleRow, _) => totpGoogleRow
        }.view.mapValues(values => values.map { case (_, attrs) => attrs })
      }.map {
        result =>
          result map {
            case (creds, attrs) =>
              val scratchCodes = attrs.map {
                attr => PasswordInfo(attr.hasher, attr.password, attr.salt)
              }
              GoogleTotpInfo(creds.sharedKey, scratchCodes)
          }
      }
  }.map(_.headOption)

  def add(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] =
    find(loginInfo).flatMap {
      case Some(foundAuthInfo) => Future.successful(foundAuthInfo)
      case None => db.run(addAction(loginInfo, authInfo))
    }

  def update(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] =
    db.run(updateAction(loginInfo, authInfo))

  def save(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => db.run(addAction(loginInfo, authInfo))
    }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
    val googleTotpInfoIdOptFut =
      db.run {
        googleTotpInfoQuery(loginInfo).map(_.id).result.headOption
      }

    googleTotpInfoIdOptFut.flatMap {
      case Some(totpId) => {
        val scratchCodesToDelete = for { sc <- totpScratchCodeTableQuery if sc.totpGoogleInfoId === totpId } yield sc
        val recordsToDelete = for { totp <- googleTotpInfoTableQuery if totp.id === totpId } yield totp

        db.run {
          DBIO.sequence(Seq(scratchCodesToDelete.delete, recordsToDelete.delete)).transactionally
        }.map(_ => ())
      }
      case None => Future.successful(())
    }
  }
}
