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
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class BlockedUserDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll with CommonTest {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/channulltest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .disable[JobModule]
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val blockedUserDAO: BlockedUserDAO = app.injector.instanceOf[BlockedUserDAO]

  val databaseAPI: DBApi = app.injector.instanceOf[DBApi]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  val testBlockingUser: User = User(
    UUID.randomUUID(),
    "blockingUser",
    Some("blocking"),
    Some("User"),
    Some("blockingUser"),
    Some("blockingUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testBlockedUser: User = User(
    UUID.randomUUID(),
    "blockedUser",
    Some("blocked"),
    Some("User"),
    Some("blockedUser"),
    Some("blockedUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testUpsertBlockedUserRequest: UpsertBlockedUserRequest = UpsertBlockedUserRequest(
    UUID.randomUUID(),
    testBlockingUser.userID,
    testBlockedUser.userID,
    Instant.now()
  )

  "BlockedUserDAO" should {
    "Upsert and get properly" in {
      (for {
        _ <- userDAO.save(testBlockingUser)
        _ <- userDAO.save(testBlockedUser)
        _ <- blockedUserDAO.upsert(testUpsertBlockedUserRequest)
        blockedUsers <- blockedUserDAO.getBlocks(testBlockingUser.userID)
      } yield blockedUsers.size must be (1)).futureValue
    }
  }

}
