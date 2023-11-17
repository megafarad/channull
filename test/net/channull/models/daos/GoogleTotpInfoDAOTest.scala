package net.channull.models.daos

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordInfo
import io.github.honeycombcheesecake.play.silhouette.impl.providers.GoogleTotpInfo
import net.channull.models.User
import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.db.DBApi
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.Instant
import java.util.UUID

class GoogleTotpInfoDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/channulltest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val loginInfoDAO: LoginInfoDAO = app.injector.instanceOf[LoginInfoDAO]
  val googleTotpInfoDAO: GoogleTotpInfoDAO = app.injector.instanceOf[GoogleTotpInfoDAO]

  val testUser: User = User(
    UUID.randomUUID(),
    "handle",
    Some("firstName"),
    Some("lastName"),
    Some("fullName"),
    Some("email@example.com"),
    None,
    None,
    Instant.now(),
    activated = true)

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  "GoogleTotpInfoDAO" should {
    "add properly" in {

      val googleTotpInfo = GoogleTotpInfo("key", Seq(PasswordInfo("hasher", "password", Some("salt"))))
      val loginInfo = LoginInfo("providerId", "providerKey")

      whenReady(userDAO.save(testUser)) {
        createdUser =>
          whenReady(loginInfoDAO.saveUserLoginInfo(createdUser.userID, loginInfo)) {
            _ =>
              whenReady(googleTotpInfoDAO.add(loginInfo, googleTotpInfo)) { addedGoogleTotpInfo =>
                googleTotpInfo must be(addedGoogleTotpInfo)
                whenReady(googleTotpInfoDAO.find(loginInfo)) {
                  foundGoogleTotpInfo => foundGoogleTotpInfo.isDefined must be(true)
                }
              }

          }
      }
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
