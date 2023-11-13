package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import net.channull.models.{ AuthToken, User, UserRole }
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.lifted.ProvenShape.proveShapeOf

import java.time.Instant
import java.util.UUID

trait AuthTableDefinitions {

  protected val profile: JdbcProfile
  import profile.api._

  implicit val userRoleColumnType: BaseColumnType[UserRole.Value] = MappedColumnType.base[UserRole.Value, String](
    e => e.toString,
    s => UserRole.withName(s)
  )

  class UserTable(tag: Tag) extends Table[User](tag, Some("auth"), "user") {
    import PostgresProfile.api._

    def id = column[UUID]("id", O.PrimaryKey)
    def handle = column[String]("handle")
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def fullName = column[Option[String]]("full_name")
    def email = column[Option[String]]("email")
    def activated = column[Boolean]("activated")
    def avatarUrl = column[Option[String]]("avatar_url")
    def signedUpAt = column[Instant]("signed_up_at")
    def profile = column[Option[String]]("profile")

    override def * : ProvenShape[User] = (id, handle, firstName, lastName, fullName, email, avatarUrl, profile,
      signedUpAt, activated).mapTo[User]

  }

  case class LoginInfoRow(id: Option[Long], providerID: String, providerKey: String)

  class LoginInfoTable(tag: Tag) extends Table[LoginInfoRow](tag, Some("auth"), "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def * = (id.?, providerID, providerKey).mapTo[LoginInfoRow]
  }

  case class UserLoginInfoRow(userID: UUID, loginInfoID: Long)

  class UserLoginInfoTable(tag: Tag) extends Table[UserLoginInfoRow](tag, Some("auth"), "user_login_info") {
    def userID = column[UUID]("user_id")
    def loginInfoID = column[Long]("login_info_id")
    def * = (userID, loginInfoID).mapTo[UserLoginInfoRow]
    def user = foreignKey("auth_user_login_info_user_id_fk", userID, userTableQuery)(_.id)
    def loginInfo = foreignKey("auth_user_login_info_login_info_id_fk", loginInfoID, loginInfoTableQuery)(_.id)
    def pk = primaryKey("auth_user_login_info_pk", (userID, loginInfoID))
  }

  case class GoogleTotpInfoRow(id: Option[Long], loginInfoId: Long, sharedKey: String)

  class GoogleTotpInfoTable(tag: Tag) extends Table[GoogleTotpInfoRow](tag, Some("auth"), "google_totp_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def loginInfoID = column[Long]("login_info_id")
    def sharedKey = column[String]("shared_key")
    def * = (id.?, loginInfoID, sharedKey).mapTo[GoogleTotpInfoRow]
    def loginInfo = foreignKey("auth_google_totp_info_login_info_id_fk", loginInfoID, loginInfoTableQuery)(_.id)
  }

  case class TotpScratchCodeRow(id: Option[Long], totpGoogleId: Long, hasher: String, password: String,
    salt: Option[String])

  class TotpScratchCodeTable(tag: Tag) extends Table[TotpScratchCodeRow](tag, Some("auth"),
    "totp_scratch_code") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def totpGoogleInfoId = column[Long]("totp_google_info_id")
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def * = (id.?, totpGoogleInfoId, hasher, password, salt).mapTo[TotpScratchCodeRow]
    def totpGoogle = foreignKey("auth_totp_scratch_code_google_totp_info_id_fk", totpGoogleInfoId, googleTotpInfoTableQuery)(_.id)
  }

  case class OAuth2InfoRow(id: Option[Long], accessToken: String, tokenType: Option[String], expiresIn: Option[Int],
    refreshToken: Option[String], loginInfoId: Long)

  class OAuth2InfoTable(tag: Tag) extends Table[OAuth2InfoRow](tag, Some("auth"), "oauth2_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def loginInfoId = column[Long]("login_info_id")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken,
      loginInfoId).mapTo[OAuth2InfoRow]
    def loginInfo = foreignKey("auth_oauth2_info_login_info_id_fk", loginInfoId, loginInfoTableQuery)(_.id)
  }

  case class PasswordInfoRow(id: Option[Long], hasher: String, password: String, salt: Option[String],
    loginInfoId: Long)

  class PasswordInfoTable(tag: Tag) extends Table[PasswordInfoRow](tag, Some("auth"), "password_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("login_info_id")
    def * = (id.?, hasher, password, salt, loginInfoId).mapTo[PasswordInfoRow]
    def loginInfo = foreignKey("auth_password_info_login_info_id_fk", loginInfoId, loginInfoTableQuery)(_.id)
  }

  class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, Some("auth"), "token") {
    import PostgresProfile.api._

    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def expiry = column[Instant]("expiry")
    def * = (id, userId, expiry).mapTo[AuthToken]
    def user = foreignKey("auth_token_user_id_fk", userId, userTableQuery)(_.id)
  }

  val userTableQuery = TableQuery[UserTable]
  val loginInfoTableQuery = TableQuery[LoginInfoTable]
  val userLoginInfoTableQuery = TableQuery[UserLoginInfoTable]
  val googleTotpInfoTableQuery = TableQuery[GoogleTotpInfoTable]
  val totpScratchCodeTableQuery = TableQuery[TotpScratchCodeTable]
  val oauth2InfoTableQuery = TableQuery[OAuth2InfoTable]
  val passwordInfoTableQuery = TableQuery[PasswordInfoTable]
  val authTokenTableQuery = TableQuery[AuthTokenTable]

  def loginInfoQuery(loginInfo: LoginInfo): Query[LoginInfoTable, LoginInfoRow, Seq] =
    loginInfoTableQuery.filter(loginInfoRow => loginInfoRow.providerID === loginInfo.providerID &&
      loginInfoRow.providerKey === loginInfo.providerKey)

}
