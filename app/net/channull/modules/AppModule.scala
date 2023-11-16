package net.channull.modules

import com.google.inject.AbstractModule
import net.channull.models.daos.{ChanNullDAO, ChanNullDAOImpl}
import net.codingwell.scalaguice.ScalaModule

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[ChanNullDAO].to[ChanNullDAOImpl]
  }

}
