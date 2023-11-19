package net.channull.modules

import com.google.inject.AbstractModule
import net.channull.models.daos._
import net.codingwell.scalaguice.ScalaModule

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[ChanNullDAO].to[ChanNullDAOImpl]
    bind[ChanNullPostDAO].to[ChanNullPostDAOImpl]
  }

}
