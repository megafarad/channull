package net.channull.models.daos

import net.channull.modules.JobModule
import net.channull.test.util.CommonTest
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ChanNullDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll with CommonTest {

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

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(500.millis))

  "ChanNullDAO" should {
    "Upsert Properly" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullDAO.upsert(testChildChanNullUpsertRequest)
        maybeChanNull <- chanNullDAO.get(testParentChanNullId)
      } yield {
        maybeChanNull.isDefined must be(true)
        val chanNull = maybeChanNull.get
        chanNull.children.size must be(1)
      }
    }

    "Get ChanNull by name" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullDAO.upsert(testChildChanNullUpsertRequest)
        maybeChanNull <- chanNullDAO.get(testChildChanNullUpsertRequest.name)
      } yield {
        maybeChanNull.isDefined must be(true)
      }
    }

    "Get Random Public ChanNull (Surf)" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullDAO.upsert(testChildChanNullUpsertRequest)
        maybeChanNull <- chanNullDAO.getRandomPublic
      } yield {
        maybeChanNull.isDefined must be (true)
      }
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
