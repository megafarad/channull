package net.channull.test.util

import net.channull.models._

import java.time.Instant
import java.util.UUID

trait CommonTest {
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
    Nil,
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
}
