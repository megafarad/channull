package net.channull.models.daos

import net.channull.models._
import net.channull.modules.JobModule
import net.channull.test.util.CommonTest
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class ReportDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll with CommonTest {

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
  val reportDAO: ReportDAO = app.injector.instanceOf[ReportDAO]

  val reportedUser: User = User(
    UUID.randomUUID(),
    "reportedUser",
    Some("reported"),
    Some("User"),
    Some("reportedUser"),
    Some("reportedUser@example.com"),
    None,
    None,
    Instant.now(),
    activated = true
  )

  val testUpsertReportedPostRequest: UpsertChanNullPostRequest = UpsertChanNullPostRequest(
    UUID.randomUUID(),
    None,
    testChanNullId,
    Some("Not kind words"),
    Instant.now(),
    reportedUser.userID,
    None
  )

  val testUpsertReportRequest: UpsertReportRequest = UpsertReportRequest(
    UUID.randomUUID(),
    testUser.userID,
    testUpsertReportedPostRequest.id,
    "Not kind",
    Seq(UpsertReportViolatedRuleRequest(
      UUID.randomUUID(),
      testChildChanNullUpsertRequest.rulesUpsertRequests.head.id
    )),
    Instant.now(),
    ReportStatus.Pending
  )



  "ReportDAO" should {
    "Upsert and get by ChanNullID properly" in {
      for {
        _ <- userDAO.save(testUser)
        _ <- chanNullDAO.upsert(testParentChanNullUpsertRequest)
        _ <- chanNullDAO.upsert(testChildChanNullUpsertRequest)
        _ <- userDAO.save(reportedUser)
        _ <- chanNullPostDAO.upsert(testUpsertReportedPostRequest)
        _ <- reportDAO.upsert(testUpsertReportRequest)
        reports <- reportDAO.getByChanNullId(testChanNullId, 0, 10)
      } yield {
        reports.items.size must be(1)
      }
    }
  }
}
