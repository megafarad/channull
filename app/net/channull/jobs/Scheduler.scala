package net.channull.jobs

import akka.actor.{ ActorRef, ActorSystem }
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
 * Schedules the jobs.
 */
class Scheduler @Inject() (
  system: ActorSystem,
  @Named("record-cleaner") recordCleaner: ActorRef) {

  QuartzSchedulerExtension(system).schedule("RecordCleaner", recordCleaner, RecordCleaner.Clean)

  recordCleaner ! RecordCleaner.Clean
}
