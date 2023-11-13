package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import io.github.honeycombcheesecake.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

class PasswordInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo] with DAOSlick {

  val classTag: ClassTag[PasswordInfo] = scala.reflect.classTag[PasswordInfo]

  import profile.api._

  protected def passwordInfoQuery(loginInfo: LoginInfo): Query[PasswordInfoTable, PasswordInfoRow, Seq] = for {
    loginInfoTable <- loginInfoQuery(loginInfo)
    passwordInfoTable <- passwordInfoTableQuery if passwordInfoTable.loginInfoId === loginInfoTable.id
  } yield passwordInfoTable

  protected def passwordInfoSubQuery(loginInfo: LoginInfo): Query[PasswordInfoTable, PasswordInfoRow, Seq] =
    passwordInfoTableQuery.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    loginInfoQuery(loginInfo).result.head.flatMap { loginInfoRow =>
      passwordInfoTableQuery += PasswordInfoRow(None, authInfo.hasher, authInfo.password, authInfo.salt,
        loginInfoRow.id.get)
    }.transactionally

  protected def updateAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    passwordInfoSubQuery(loginInfo)
      .map(passwordInfoTable => (passwordInfoTable.hasher, passwordInfoTable.password, passwordInfoTable.salt))
      .update((authInfo.hasher, authInfo.password, authInfo.salt))

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(passwordInfoQuery(loginInfo).result.headOption).map { passwordInfoRowOpt =>
      passwordInfoRowOpt map {
        passwordInfoRow => PasswordInfo(passwordInfoRow.hasher, passwordInfoRow.password, passwordInfoRow.salt)
      }
    }
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = loginInfoQuery(loginInfo).joinLeft(passwordInfoTableQuery).on(_.id === _.loginInfoId)
    val action = query.result.head.flatMap {
      case (_, Some(_)) => updateAction(loginInfo, authInfo)
      case (_, None) => addAction(loginInfo, authInfo)
    }
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(passwordInfoSubQuery(loginInfo).delete).map(_ => ())
}
