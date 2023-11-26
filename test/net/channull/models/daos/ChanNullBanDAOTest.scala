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

class ChanNullBanDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll
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
  val chanNullBanDAO: ChanNullBanDAO = app.injector.instanceOf[ChanNullBanDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  val testBanningUser: User = User(
    UUID.randomUUID(),
    "banningUser",
    Some("banning"),
    Some("user"),
    Some("banningUser"),
    Some("banningUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testBannedUser: User = User(
    UUID.randomUUID(),
    "bannedUser",
    Some("banned"),
    Some("user"),
    Some("bannedUser"),
    Some("bannedUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testUpsertChanNullBanRequest: UpsertChanNullBanRequest = UpsertChanNullBanRequest(
    id = UUID.randomUUID(), chanNullId = testChanNullId, userId = testBannedUser.userID,
    bannedByUserId = testBanningUser.userID, reason = Some("Not kind"), whenCreated = Instant.now(), expiry = None,
    violatedRules = Seq(UpsertChanNullBanViolatedRuleRequest(
      UUID.randomUUID(),
      testChildChanNullUpsertRequest.rulesUpsertRequests.head.id
    ))
  )

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(500.millis))

  "ChanNullBanDAO" should {
    "Upsert and get by ChanNullID properly" in {
      whenReady(userDAO.save(testUser)) {
        _ => whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest)) {
          _ => whenReady(chanNullDAO.upsert(testChildChanNullUpsertRequest)) {
            _ => whenReady(userDAO.save(testBanningUser)) {
              _ => whenReady(userDAO.save(testBannedUser)) {
                _ => whenReady(chanNullBanDAO.upsert(testUpsertChanNullBanRequest)) {
                  _ => whenReady(chanNullBanDAO.getByChanNullId(testChanNullId)) {
                    bans => bans.size must be(1)
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
