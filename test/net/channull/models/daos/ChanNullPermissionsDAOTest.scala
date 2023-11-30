package net.channull.models.daos

import net.channull.models.{ChanNullPermissions, UserRole}
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

class ChanNullPermissionsDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll
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
  val chanNullPermissionsDAO: ChanNullPermissionsDAO = app.injector.instanceOf[ChanNullPermissionsDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  val testChanNullPermissionsId: UUID = UUID.randomUUID()
  val testChanNullPermissions: ChanNullPermissions = ChanNullPermissions(id = testChanNullPermissionsId,
    chanNullId = testParentChanNullId, role = UserRole.User, canPost = true, canSubPost = true, canBan = false)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(500.millis))

  "ChanNullPermissionsDAO" should {
    "Upsert and get properly" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullPermissionsDAO.upsert(testChanNullPermissions)
        chanNullPermissions <- chanNullPermissionsDAO.getByChanNullId(testParentChanNullId)
      } yield {
        chanNullPermissions.size must be(1)
      }
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
