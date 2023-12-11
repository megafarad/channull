package net.channull.models.daos

import net.channull.models._
import net.channull.modules.JobModule
import net.channull.test.util.CommonTest
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ChanNullUserDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll
  with CommonTest {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/channulltest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .disable[JobModule]
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val chanNullDAO: ChanNullDAO = app.injector.instanceOf[ChanNullDAO]
  val chanNullUserDAO: ChanNullUserDAO = app.injector.instanceOf[ChanNullUserDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  val testUpsertChanNullUser: UpsertChanNullUserRequest = UpsertChanNullUserRequest(
    testParentChanNullId,
    testUser.userID,
    UserRole.Admin,
    None
  )

  val testRegularUser: User = User(
    UUID.randomUUID(),
    "testRegularUser",
    Some("test"),
    Some("User"),
    Some("testRegularUser"),
    Some("testRegularUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testUpsertRegularUser: UpsertChanNullUserRequest = UpsertChanNullUserRequest(
    testParentChanNullId,
    testRegularUser.userID,
    UserRole.User,
    None
  )

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  "ChanNullUserDAO" should {
    "Upsert and get by ChanNullID properly" in {
      (for {
        _ <- userDAO.save(testUser)
        _ <- userDAO.save(testRegularUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullUserDAO.upsert(testUpsertChanNullUser)
        _ <- chanNullUserDAO.upsert(testUpsertRegularUser)
        chanNullUsers <- chanNullUserDAO.getChanNullUsers(testParentChanNullId, 0, 10)
      } yield {
        chanNullUsers.items.size must be(2)
        chanNullUsers.total must be(2)
        chanNullUsers.items.map(_.role).head must be(UserRole.Admin)
        chanNullUsers.items.map(_.role)(1) must be(UserRole.User)
      }).futureValue
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
