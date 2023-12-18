package net.channull.modules

import net.channull.jobs.{ RecordCleaner, Scheduler }
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * The job module.
 */
class JobModule extends ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bindActor[RecordCleaner]("record-cleaner")
    bind[Scheduler].asEagerSingleton()
  }
}
