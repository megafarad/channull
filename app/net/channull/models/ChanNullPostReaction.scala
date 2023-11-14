package net.channull.models

import java.util.UUID

case class ChanNullPostReaction(id: UUID, postId: UUID, user: User, reactionType: String)
