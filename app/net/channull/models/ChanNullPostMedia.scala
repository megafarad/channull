package net.channull.models

import java.util.UUID

case class ChanNullPostMedia(id: UUID, postId: UUID, altText: Option[String], contentType: String, contentUrl: String,
  contentSize: Long)
