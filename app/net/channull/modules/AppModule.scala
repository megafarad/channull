package net.channull.modules

import com.google.inject.AbstractModule
import net.channull.models.daos._
import net.codingwell.scalaguice.ScalaModule

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[ChanNullDAO].to[ChanNullDAOImpl]
    bind[ChanNullPostDAO].to[ChanNullPostDAOImpl]
    bind[ChanNullPostMediaDAO].to[ChanNullPostMediaDAOImpl]
    bind[ChanNullPermissionsDAO].to[ChanNullPermissionsDAOImpl]
    bind[ChanNullBanDAO].to[ChanNullBanDAOImpl]
    bind[ChanNullUserDAO].to[ChanNullUserDAOImpl]
    bind[ReportDAO].to[ReportDAOImpl]
  }

}
