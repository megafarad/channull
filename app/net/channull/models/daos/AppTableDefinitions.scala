package net.channull.models.daos

import net.channull.models.{ChanNullAccess, ChanNullPermissions, ChanNullPostMedia, ReportStatus, UserRole}

import java.time.Instant
import java.util.UUID

trait AppTableDefinitions { self: AuthTableDefinitions =>

  import profile.api._

  implicit val chanNullAccessType: BaseColumnType[ChanNullAccess.Value] =
    MappedColumnType.base[ChanNullAccess.Value, String](
      e => e.toString,
      s => ChanNullAccess.withName(s)
    )

  implicit val reportStatusType: BaseColumnType[ReportStatus.Value] = MappedColumnType.base[ReportStatus.Value, String](
    e => e.toString,
    s => ReportStatus.withName(s)
  )

  val random: Rep[Double] = SimpleFunction.nullary[Double]("random")

  case class ChanNullRow(id: UUID, parentId: Option[UUID], name: String, description: String, whenCreated: Instant,
    whoCreated: UUID, access: ChanNullAccess.Value)

  class ChanNullTable(tag: Tag) extends Table[ChanNullRow](tag, Some("app"), "channull") {
    import PostgresProfile.api._

    def id = column[UUID]("id", O.PrimaryKey)
    def parentId = column[Option[UUID]]("parent_id")
    def name = column[String]("name", O.Unique)
    def description = column[String]("description")
    def whenCreated = column[Instant]("when_created")
    def whoCreated = column[UUID]("who_created")
    def access = column[ChanNullAccess.Value]("access")
    def whoCreatedUser = foreignKey("app_channull_who_created_fk", whoCreated, userTableQuery)(_.id)
    def parent = foreignKey("app_channull_parent_id_fk", parentId, TableQuery[ChanNullTable])(_.id.?)
    def * = (id, parentId, name, description, whenCreated, whoCreated, access).mapTo[ChanNullRow]
  }

  val chanNullTableQuery = TableQuery[ChanNullTable]

  val randomPublicChanNullQuery = chanNullTableQuery.filter(_.access === ChanNullAccess.Public)
    .filter(_.parentId.isEmpty).sortBy(_ => random).take(1)

  case class ChanNullRuleRow(id: UUID, chanNullID: UUID, number: Short, rule: String, whenCreated: Instant,
    whoCreated: UUID)

  class ChanNullRuleTable(tag: Tag) extends Table[ChanNullRuleRow](tag, Some("app"), "channull_rule") {
    def id = column[UUID]("id", O.PrimaryKey)
    def chanNullId = column[UUID]("channull_id")
    def number = column[Short]("number")
    def rule = column[String]("rule")
    def whenCreated = column[Instant]("when_created")
    def whoCreated = column[UUID]("who_created")
    def chanNull = foreignKey("app_channull_rule_channull_id_fk", chanNullId, chanNullTableQuery)(_.id)
    def whoCreatedUser = foreignKey("app_channull_rule_who_created_fk", whoCreated, userTableQuery)(_.id)
    def * = (id, chanNullId, number, rule, whenCreated, whoCreated).mapTo[ChanNullRuleRow]
  }

  val chanNullRuleTableQuery = TableQuery[ChanNullRuleTable]

  class ChanNullPermissionsTable(tag: Tag) extends Table[ChanNullPermissions](tag, Some("app"),
    "channull_permissions") {
    def id = column[UUID]("id", O.PrimaryKey)
    def chanNullId = column[UUID]("channull_id")
    def role = column[UserRole.Value]("role")
    def canPost = column[Boolean]("can_post")
    def canSubPost = column[Boolean]("can_subpost")
    def canBan = column[Boolean]("can_ban")
    def chanNull = foreignKey("app_channull_permissions_channull_id_fk", chanNullId, chanNullTableQuery)(_.id)
    def idx = index("app_channull_permissions_unique", (chanNullId, role), unique = true)
    def * = (id, chanNullId, role, canPost, canSubPost, canBan).mapTo[ChanNullPermissions]
  }

  val chanNullPermissionsTableQuery = TableQuery[ChanNullPermissionsTable]

  case class ChanNullBanRow(id: UUID, chanNullId: UUID, userId: UUID, bannedBy: UUID, reason: Option[String],
    whenCreated: Instant, expiry: Option[Instant])

  class ChanNullBanTable(tag: Tag) extends Table[ChanNullBanRow](tag, Some("app"), "channull_ban") {
    def id = column[UUID]("id", O.PrimaryKey)
    def chanNullId = column[UUID]("channull_id")
    def userId = column[UUID]("user_id")
    def bannedBy = column[UUID]("banned_by")
    def reason = column[Option[String]]("reason")
    def whenCreated = column[Instant]("when_created")
    def expiry = column[Option[Instant]]("expiry")
    def chanNull = foreignKey("app_channull_ban_channull_id_fk", chanNullId, chanNullTableQuery)(_.id)
    def bannedUser = foreignKey("app_channull_ban_user_id_fk", userId, userTableQuery)(_.id)
    def bannedByUser = foreignKey("app_channull_ban_banned_by_fk", bannedBy, userTableQuery)(_.id)
    def * = (id, chanNullId, userId, bannedBy, reason, whenCreated, expiry).mapTo[ChanNullBanRow]
  }

  val chanNullBanTableQuery = TableQuery[ChanNullBanTable]

  case class ChanNullBanViolatedRuleRow(id: UUID, banId: UUID, violatedRuleId: UUID)

  class ChanNullBanViolatedRuleTable(tag: Tag) extends Table[ChanNullBanViolatedRuleRow](tag, Some("app"),
    "channull_ban_violated_rule") {
    def id = column[UUID]("id", O.PrimaryKey)
    def banId = column[UUID]("ban_id")
    def violatedRuleId = column[UUID]("violated_rule_id")
    def ban = foreignKey("app_channull_ban_violated_rule_ban_id", banId, chanNullBanTableQuery)(_.id)
    def violatedRule = foreignKey("app_channull_ban_violated_rule_rule_id", violatedRuleId, chanNullRuleTableQuery)(_.id)
    def * = (id, banId, violatedRuleId).mapTo[ChanNullBanViolatedRuleRow]
  }

  val chanNullBanViolatedRuleTableQuery = TableQuery[ChanNullBanViolatedRuleTable]

  case class ChanNullPostRow(id: UUID, parentId: Option[UUID], chanNullId: UUID, text: Option[String],
    whenCreated: Instant, whoCreated: UUID, expiry: Option[Instant])

  class ChanNullPostTable(tag: Tag) extends Table[ChanNullPostRow](tag, Some("app"), "channull_post") {
    def id = column[UUID]("id", O.PrimaryKey)
    def parentId = column[Option[UUID]]("parent_id")
    def chanNullId = column[UUID]("channull_id")
    def text = column[Option[String]]("text")
    def whenCreated = column[Instant]("when_created")
    def whoCreated = column[UUID]("who_created")
    def expiry = column[Option[Instant]]("expiry")
    def chanNull = foreignKey("app_channull_post_channull_id_fk", chanNullId, chanNullTableQuery)(_.id)
    def whoCreatedUser = foreignKey("app_channull_post_who_created_fk", whoCreated, userTableQuery)(_.id)
    def parent = foreignKey("app_channull_post_parent_id_fk", parentId, TableQuery[ChanNullPostTable])(_.id.?)
    def * = (id, parentId, chanNullId, text, whenCreated, whoCreated,
      expiry).mapTo[ChanNullPostRow]
  }

  val chanNullPostTableQuery = TableQuery[ChanNullPostTable]

  class ChanNullPostMediaTable(tag: Tag) extends Table[ChanNullPostMedia](tag, Some("app"),
    "channull_post_media") {
    def id = column[UUID]("id", O.PrimaryKey)
    def postId = column[UUID]("post_id")
    def altText = column[Option[String]]("alt_text")
    def contentType = column[String]("content_type")
    def contentUrl = column[String]("content_url")
    def contentSize = column[Long]("content_size")
    def post = foreignKey("app_channull_post_media_post_id_fk", postId, chanNullPostTableQuery)(_.id)
    def * = (id, postId, altText, contentType, contentUrl, contentSize).mapTo[ChanNullPostMedia]
  }

  val chanNullPostMediaTableQuery = TableQuery[ChanNullPostMediaTable]

  case class ChanNullPostReactionRow(id: UUID, postId: UUID, userID: UUID, reactionType: String, timestamp: Instant)

  class ChanNullPostReactionTable(tag: Tag) extends Table[ChanNullPostReactionRow](tag, Some("app"),
    "channull_post_reaction") {
    def id = column[UUID]("id", O.PrimaryKey)
    def postId = column[UUID]("post_id")
    def userId = column[UUID]("user_id")
    def reactionType = column[String]("reaction_type")
    def timestamp = column[Instant]("timestamp")
    def * = (id, postId, userId, reactionType, timestamp).mapTo[ChanNullPostReactionRow]
  }

  val chanNullPostReactionTableQuery = TableQuery[ChanNullPostReactionTable]

  case class UserChanNullRow(id: UUID, userId: UUID, chanNullId: UUID, role: UserRole.Value)

  class UserChanNullTable(tag: Tag) extends Table[UserChanNullRow](tag, Some("app"), "user_channull") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def chanNullId = column[UUID]("channull_id")
    def role = column[UserRole.Value]("role")
    def user = foreignKey("app_user_channull_user_id_fk", userId, userTableQuery)(_.id)
    def chanNull = foreignKey("app_user_channull_channull_id_fk", chanNullId, chanNullTableQuery)(_.id)
    def * = (id, userId, chanNullId, role).mapTo[UserChanNullRow]
  }

  val userChanNullTableQuery = TableQuery[UserChanNullTable]

  case class ReportRow(id: UUID, reporter: UUID, postId: UUID, report: String, timestamp: Instant,
    status: ReportStatus.Value)

  class ReportTable(tag: Tag) extends Table[ReportRow](tag, Some("app"), "report") {
    def id = column[UUID]("id", O.PrimaryKey)
    def reporter = column[UUID]("reporter")
    def postId = column[UUID]("post_id")
    def report = column[String]("report")
    def timestamp = column[Instant]("timestamp")
    def status = column[ReportStatus.Value]("status")
    def reporterUser = foreignKey("app_report_reporter_fk", reporter, userTableQuery)(_.id)
    def post = foreignKey("app_report_post_id_fk", postId, chanNullPostTableQuery)(_.id)
    def * = (id, reporter, postId, report, timestamp, status).mapTo[ReportRow]
  }

  val reportTableQuery = TableQuery[ReportTable]

  case class ReportViolatedRuleRow(id: UUID, reportId: UUID, violatedRuleId: UUID)

  class ReportViolatedRuleTable(tag: Tag) extends Table[ReportViolatedRuleRow](tag, Some("app"),
    "report_violated_rule") {
    def id = column[UUID]("id", O.PrimaryKey)
    def reportId = column[UUID]("report_id")
    def violatedRuleId = column[UUID]("violated_rule_id")
    def report = foreignKey("app_report_violated_rule_report_id_fk", reportId, reportTableQuery)(_.id)
    def violatedRule = foreignKey("app_report_violated_rule_violated_rule_id", violatedRuleId, chanNullRuleTableQuery)(_.id)
    def * = (id, reportId, violatedRuleId).mapTo[ReportViolatedRuleRow]
  }

  val reportViolatedRuleTableQuery = TableQuery[ReportViolatedRuleTable]

  case class BlockedUserRow(id: UUID, blockingUserId: UUID, blockedUserId: UUID, timestamp: Instant)

  class BlockedUserTable(tag: Tag) extends Table[BlockedUserRow](tag, Some("app"), "blocked_user") {
    def id = column[UUID]("id", O.PrimaryKey)
    def blockingUserId = column[UUID]("blocking_user_id")
    def blockedUserId = column[UUID]("blocked_user_id")
    def timestamp = column[Instant]("timestamp")
    def blockingUser = foreignKey("app_blocked_user_blocking_user_id_fk", blockingUserId, userTableQuery)(_.id)
    def blockedUser = foreignKey("app_blocked_user_blocked_user_id_fk", blockedUserId, userTableQuery)(_.id)
    def * = (id, blockingUserId, blockedUserId, timestamp).mapTo[BlockedUserRow]
  }

  val blockedUserTableQuery = TableQuery[BlockedUserTable]
}

