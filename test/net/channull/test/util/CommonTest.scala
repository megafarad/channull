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
}
