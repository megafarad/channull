package net.channull.models.daos

import net.channull.models._
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

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration._

class ChanNullPostDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll
  with CommonTest {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/channulltest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val chanNullDAO: ChanNullDAO = app.injector.instanceOf[ChanNullDAO]
  val chanNullPostDAO: ChanNullPostDAO = app.injector.instanceOf[ChanNullPostDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  val testChanNullPostID: UUID = UUID.randomUUID()
  val testChildChanNullPostID: UUID = UUID.randomUUID()
  val testSecondChildChanNullPostId: UUID = UUID.randomUUID()
  val testGrandChildChanNullPostID: UUID = UUID.randomUUID()

  val testUpsertChanNullPostRequest: UpsertChanNullPostRequest = UpsertChanNullPostRequest(
    id = testChanNullPostID, parentId = None, chanNullId = testParentChanNullId, text = Some("Be kind"),
    whenCreated = Instant.now(), whoCreated = testUser.userID, expiry = None)
  val testUpsertChildChanNullPostRequest: UpsertChanNullPostRequest = UpsertChanNullPostRequest(
    id = testChildChanNullPostID, parentId = Some(testChanNullPostID), chanNullId = testParentChanNullId,
    text = Some("Be kind"), whenCreated = Instant.now(), whoCreated = testUser.userID, expiry = None
  )
  val testUpsertSecondChildChanNullPostRequest: UpsertChanNullPostRequest = UpsertChanNullPostRequest(
    id = testSecondChildChanNullPostId, parentId = Some(testChanNullPostID), chanNullId = testParentChanNullId,
    text = Some("Be kind"), whenCreated = Instant.now(), whoCreated = testUser.userID, expiry = None
  )
  val testUpsertGrandChildChanNullPostRequest: UpsertChanNullPostRequest = UpsertChanNullPostRequest(
    id = testGrandChildChanNullPostID, parentId = Some(testChildChanNullPostID), chanNullId = testParentChanNullId,
    text = Some("Be kind"), whenCreated = Instant.now(), whoCreated = testUser.userID, expiry = None
  )

  "ChanNullPostDAO" should {
    "Upsert Properly" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest), Timeout(1.minute)) {
            _ =>
              whenReady(chanNullPostDAO.upsert(testUpsertChanNullPostRequest), Timeout(1.minute)) {
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
                                println(post)
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
