package net.channull.models.daos

import net.channull.models._
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

class ChanNullDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/channulltest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val chanNullDAO: ChanNullDAO = app.injector.instanceOf[ChanNullDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

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
    activated = true
  )

  val testChanNullId: UUID = UUID.randomUUID()
  val testParentChanNullId: UUID = UUID.randomUUID()

  val testParentChanNullUpsertRequest: UpsertChanNullRequest = UpsertChanNullRequest(
    testParentChanNullId,
    None,
    "/c/parent",
    testUser.userID,
    "test parent ChanNull",
    Seq(
      UpsertChanNullRuleRequest(
        UUID.randomUUID(),
        testParentChanNullId,
        1,
        "be kind",
        Instant.now(),
        testUser.userID
      )
    ),
    Instant.now(),
    ChanNullAccess.Public
  )

  val testChildChanNullUpsertRequest: UpsertChanNullRequest = UpsertChanNullRequest(
    testChanNullId,
    Some(testParentChanNullId),
    "/c/parent/child",
    testUser.userID,
    "Test child ChanNull",
    Seq(
      UpsertChanNullRuleRequest(
        UUID.randomUUID(),
        testChanNullId,
        1,
        "be kind",
        Instant.now(),
        testUser.userID
      )
    ),
    Instant.now(),
    ChanNullAccess.Public
  )

  "ChanNullDAO" should {
    "Upsert Properly" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest)) {
            _ =>
              whenReady(chanNullDAO.upsert(testChildChanNullUpsertRequest)) {
                upsertedChild =>
                  upsertedChild.rules.size must be(1)
                  upsertedChild.parent.isDefined must be(true)
                  upsertedChild.parent.get.creator.userID must be(testUser.userID)
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
