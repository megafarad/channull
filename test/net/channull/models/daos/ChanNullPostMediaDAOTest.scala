package net.channull.models.daos

import net.channull.models.ChanNullPostMedia
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

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

class ChanNullPostMediaDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll 
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
  val chanNullPostDAO: ChanNullPostDAO = app.injector.instanceOf[ChanNullPostDAO]
  val chanNullPostMediaDAO: ChanNullPostMediaDAO = app.injector.instanceOf[ChanNullPostMediaDAO]
  
  val databaseApi: DBApi = app.injector.instanceOf[DBApi]
  
  val testChanNullPostMediaId: UUID = UUID.randomUUID()
  val testChanNullPostMedia: ChanNullPostMedia = ChanNullPostMedia(
    id = testChanNullPostMediaId, postId = testChanNullPostID, altText = Some("altText"), contentType = "image/png",
    contentUrl = "https://example.com/test.png", contentSize = 100
  )

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(500.millis))

  "ChanNullPostMediaDAO" should {
    "Upsert and get properly" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullPostDAO.upsert(testUpsertChanNullPostRequest)
        _ <- chanNullPostMediaDAO.upsert(testChanNullPostMedia)
        maybeChanNullPostMedia <- chanNullPostMediaDAO.get(testChanNullPostMediaId)
        _ = maybeChanNullPostMedia.isDefined must be (true)
        mediaForPost <- chanNullPostMediaDAO.getMediaForPost(testChanNullPostID)
      } yield mediaForPost.size must be (1)
    }

    "Delete properly" in {
      for {
        _ <- chanNullPostMediaDAO.delete(testChanNullPostMediaId)
        maybePostMedia <- chanNullPostMediaDAO.get(testChanNullPostMediaId)
      } yield maybePostMedia.isDefined must be (false)
    }
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeAll()
  }
  
}
