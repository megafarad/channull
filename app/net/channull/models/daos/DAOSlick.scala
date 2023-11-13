package net.channull.models.daos

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait DAOSlick extends AuthTableDefinitions with AppTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]
