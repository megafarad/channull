package net.channull.models.daos

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

class ChanNullDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterAll with CommonTest {

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

  "ChanNullDAO" should {
    "Upsert Properly" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest), Timeout(1.minute)) {
            _ =>
              whenReady(chanNullDAO.upsert(testChildChanNullUpsertRequest), Timeout(1.minute)) {
                upsertedChild =>
                  upsertedChild.rules.size must be(1)
                  upsertedChild.parent.isDefined must be(true)
                  upsertedChild.parent.get.creator.userID must be(testUser.userID)
              }
          }
      }
    }

    "Get ChanNull by name" in {
      whenReady(userDAO.save(testUser)) {
        _ => whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest), Timeout(1.minute)) {
          _ => whenReady(chanNullDAO.upsert(testChildChanNullUpsertRequest), Timeout(1.minute)) {
            _ => whenReady(chanNullDAO.get(testChildChanNullUpsertRequest.name)) {
              maybeChanNull =>
                maybeChanNull.isDefined must be(true)
            }
          }
        }
      }
    }

    "Get Random Public ChanNull (Surf)" in {
      whenReady(userDAO.save(testUser)) {
        _ =>
          whenReady(chanNullDAO.upsert(testParentChanNullUpsertRequest), Timeout(1.minute)) {
            _ =>
              whenReady(chanNullDAO.upsert(testChildChanNullUpsertRequest), Timeout(1.minute)) {
                _ =>
                  whenReady(chanNullDAO.getRandomPublic) {
                    maybeChanNull =>
                      maybeChanNull.isDefined must be(true)
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
