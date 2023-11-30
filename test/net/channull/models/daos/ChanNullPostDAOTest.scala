package net.channull.models.daos

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

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ChanNullPostDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll
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

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  private def setupTestData: Future[Unit] = for {
    _ <- userDAO.save(testUser)
    _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
    _ <- chanNullPostDAO.upsert(testUpsertChanNullPostRequest)
    _ <- chanNullPostDAO.upsert(testUpsertChildChanNullPostRequest)
    _ <- chanNullPostDAO.upsert(testUpsertSecondChildChanNullPostRequest)
    _ <- chanNullPostDAO.upsert(testUpsertGrandChildChanNullPostRequest)
  } yield ()

  "ChanNullPostDAO" should {
    "Upsert properly" in {
      whenReady(setupTestData) {
        _ => whenReady(chanNullPostDAO.getPost(testChanNullPostID)) {
          maybeChanNullPost =>
            maybeChanNullPost.isDefined must be (true)
            val post = maybeChanNullPost.get
            post.children.size must be (2)
        }
      }
    }

    "Delete properly" in {
      whenReady(setupTestData) {
        _ => whenReady(chanNullPostDAO.delete(testChanNullPostID)) {
          _ => whenReady(chanNullPostDAO.getPost(testChanNullPostID)) {
            post =>
              post.isDefined must be(false)
              whenReady(chanNullPostDAO.getPost(testChildChanNullPostID)) {
                childPost =>
                  childPost.isDefined must be(false)
                  whenReady(chanNullPostDAO.getPost(testSecondChildChanNullPostId)) {
                    secondChildPost =>
                      secondChildPost.isDefined must be(false)
                      whenReady(chanNullPostDAO.getPost(testGrandChildChanNullPostID)) {
                        grandchildPost =>
                          grandchildPost.isDefined must be(false)
                      }
                  }
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
