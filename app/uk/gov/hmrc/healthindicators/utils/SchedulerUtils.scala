/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.healthindicators.utils

import akka.actor.ActorSystem
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.mongo.lock.LockService
import uk.gov.hmrc.healthindicators.configs.SchedulerConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait SchedulerUtils {

  private val logger = Logger(this.getClass)

  def schedule(
    label: String,
    schedulerConfig: SchedulerConfig
  )(f: => Future[Unit])(implicit
    actorSystem: ActorSystem,
    applicationLifecycle: ApplicationLifecycle,
    ec: ExecutionContext
  ): Unit =
    if (schedulerConfig.enabled) {

      val initialDelay = schedulerConfig.initialDelay()

      val frequency = schedulerConfig.frequency()
      logger.info(s"Enabling $label scheduler, running every $frequency (after initial delay $initialDelay)")

      val cancellable =
        actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, frequency) { () =>
          logger.info(s"Running $label scheduler")
          f.recover {
            case e => logger.error(s"$label interrupted because: ${e.getMessage}", e)
          }
        }

      applicationLifecycle.addStopHook(() => Future(cancellable.cancel()))
    } else
      logger.info(
        s"$label scheduler is DISABLED. to enable, configure configure ${schedulerConfig.enabledKey}=true in config."
      )

  def scheduleWithLock(
    label: String,
    schedulerConfig: SchedulerConfig,
    lock: LockService
  )(f: => Future[Unit])(implicit
    actorSystem: ActorSystem,
    applicationLifecycle: ApplicationLifecycle,
    ec: ExecutionContext
  ): Unit =
    schedule(label, schedulerConfig) {

      lock
        .withLock(f)
        .map {
          case Some(_) => logger.info(s"$label finished - releasing lock")
          case None    => logger.info(s"$label cannot run - lock ${lock.lockId} is taken... skipping update")
        }
        .recover {
          case NonFatal(e) => logger.error(s"$label interrupted because: ${e.getMessage}", e)
        }
    }
}

object SchedulerUtils extends SchedulerUtils
