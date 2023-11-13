package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.OAuth2Info
import io.github.honeycombcheesecake.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

class OAuth2InfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[OAuth2Info] with DAOSlick {

  val classTag: ClassTag[OAuth2Info] = scala.reflect.classTag[OAuth2Info]

  import profile.api._

  protected def oauth2InfoQuery(loginInfo: LoginInfo): Query[OAuth2InfoTable, OAuth2InfoRow, Seq] = for {
    loginInfoTable <- loginInfoQuery(loginInfo)
    oauthInfo2Table <- oauth2InfoTableQuery if oauthInfo2Table.loginInfoId === loginInfoTable.id
  } yield oauthInfo2Table

  // Use subquery workaround instead of join to get authinfo because slick only supports selecting
  // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
  protected def oauth2InfoSubQuery(loginInfo: LoginInfo): Query[OAuth2InfoTable, OAuth2InfoRow, Seq] =
    oauth2InfoTableQuery.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    loginInfoQuery(loginInfo).result.head.flatMap { loginInfoRow =>
      oauth2InfoTableQuery += OAuth2InfoRow(
        None,
        authInfo.accessToken,
        authInfo.tokenType,
        authInfo.expiresIn,
        authInfo.refreshToken,
        loginInfoRow.id.get
      )
    }.transactionally
  }

  protected def updateAction(loginInfo: LoginInfo, authInfo: OAuth2Info) =
    oauth2InfoSubQuery(loginInfo)
      .map(oauth2InfoTable => (oauth2InfoTable.accessToken, oauth2InfoTable.tokenType, oauth2InfoTable.expiresIn,
        oauth2InfoTable.refreshToken))
      .update((authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken))

  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val result = db.run(oauth2InfoQuery(loginInfo).result.headOption)
    result.map { oauth2InfoRowOpt =>
      oauth2InfoRowOpt.map { oauth2InfoRow =>
        OAuth2Info(oauth2InfoRow.accessToken, oauth2InfoRow.tokenType, oauth2InfoRow.expiresIn,
          oauth2InfoRow.refreshToken)
      }
    }
  }

  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val query = for {
      result <- loginInfoQuery(loginInfo).joinLeft(oauth2InfoTableQuery).on(_.id === _.loginInfoId)
    } yield result
    val action = query.result.head.flatMap {
      case (_, Some(_)) => updateAction(loginInfo, authInfo)
      case (_, None) => addAction(loginInfo, authInfo)
    }
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(oauth2InfoSubQuery(loginInfo).delete).map(_ => ())
}
