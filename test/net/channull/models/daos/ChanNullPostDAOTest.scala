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

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(500.millis))

  "ChanNullPostDAO" should {
    "Upsert properly" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest)) {
            _ =>
              whenReady(chanNullPostDAO.upsert(testUpsertChanNullPostRequest)) {
                upsertedPost =>
                  upsertedPost.children.isEmpty must be (true)
                  upsertedPost.reactions.isEmpty must be (true)
                  whenReady(chanNullPostDAO.upsert(testUpsertChildChanNullPostRequest)) {
                    _ =>
                      whenReady(chanNullPostDAO.upsert(testUpsertSecondChildChanNullPostRequest)) {
                        _ =>
                          whenReady(chanNullPostDAO.upsert(testUpsertGrandChildChanNullPostRequest)) {
                            _ => whenReady(chanNullPostDAO.getPost(testChanNullPostID)) {
                              maybeChanNullPost =>
                                maybeChanNullPost.isDefined must be (true)
                                val post = maybeChanNullPost.get
                                post.children.size must be (2)
                            }
                          }
                      }
                  }
              }
          }
      }
    }

    "Delete properly" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest)) {
            _ =>
              whenReady(chanNullPostDAO.upsert(testUpsertChanNullPostRequest)) {
                _ =>
                  whenReady(chanNullPostDAO.upsert(testUpsertChildChanNullPostRequest)) {
                    _ =>
                      whenReady(chanNullPostDAO.upsert(testUpsertSecondChildChanNullPostRequest)) {
                        _ =>
                          whenReady(chanNullPostDAO.upsert(testUpsertGrandChildChanNullPostRequest)) {
                            _ =>
                              whenReady(chanNullPostDAO.getPost(testChanNullPostID)) {
                                maybePost =>
                                  maybePost.isDefined must be(true)
                                  whenReady(chanNullPostDAO.delete(testChanNullPostID)) {
                                    _ =>
                                      whenReady(chanNullPostDAO.getPost(testChanNullPostID)) {
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
