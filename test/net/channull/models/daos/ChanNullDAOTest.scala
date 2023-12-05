package net.channull.models.daos

import net.channull.models.ChanNull
import net.channull.modules.JobModule
import net.channull.test.util.CommonTest
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Logging}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ChanNullDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll with CommonTest with Logging {

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

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  def setup: Future[Unit] = for {
    _ <- userDAO.save(testUser)
    _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
    _ <- chanNullDAO.upsert(testChildChanNullUpsertRequest)
  } yield ()

  "ChanNullDAO" should {
    "Upsert Properly" in {
      val upsertedChanNull  = for {
        _ <- setup
        maybeChanNull <- chanNullDAO.get(testParentChanNullId)
      } yield maybeChanNull

      GetChanNullAssertion(upsertedChanNull)(_.children.size must be (1))
    }

    "Get ChanNull by name" in {
      val chanNullByName = for {
        _ <- setup
        maybeChanNull <- chanNullDAO.get(testChildChanNullUpsertRequest.name)
      } yield maybeChanNull

      GetChanNullAssertion(chanNullByName)()
    }

    "Get Random Public ChanNull (Surf)" in {
      val randomPublicChanNull = for {
        _ <- setup
        maybeChanNull <- chanNullDAO.getRandomPublic
      } yield maybeChanNull

      GetChanNullAssertion(randomPublicChanNull)()
    }

    "Search name field" in {
      val foundChanNulls = for {
        _ <- setup
        chanNulls <- chanNullDAO.search("parent", 0, 10)
      } yield chanNulls

      whenReady(foundChanNulls) {
        chanNullsPage =>
          logger.info(chanNullsPage.toString)
          chanNullsPage.items.size must be (2)
      }
    }
  }

  private def GetChanNullAssertion(chanNull: Future[Option[ChanNull]])(extraAssertions: ChanNull => Unit = _ => ()):
  Unit = whenReady(chanNull) { maybeChanNull =>
    maybeChanNull.isDefined must be (true)
    maybeChanNull.foreach(extraAssertions)
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }

}
