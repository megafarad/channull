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
    UUID.randomUUID(),
    testParentChanNullId,
    testUser.userID,
    UserRole.Admin,
    None
  )

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  "ChanNullUserDAO" should {
    "Upsert and get by ChanNullID properly" in {
      (for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullUserDAO.upsert(testUpsertChanNullUser)
        chanNullUsers <- chanNullUserDAO.getChanNullUsers(testParentChanNullId, 0, 10)
      } yield {
        chanNullUsers.items.size must be(1)
      }).futureValue
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
