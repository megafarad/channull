package net.channull.models

import java.time.Instant
import java.util.UUID

case class UpsertChanNullRequest(id: UUID, parentId: Option[UUID], name: String, creator: UUID, description: String,
                                 rulesUpsertRequests: Seq[UpsertChanNullRuleRequest], whenCreated: Instant,
                                 access: ChanNullAccess.Value)

case class UpsertChanNullRuleRequest(id: UUID, chanNullId: UUID, number: Short, rule: String, whenCreated: Instant,
                                     whoCreated: UUID)
