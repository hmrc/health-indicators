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

package uk.gov.hmrc.healthindicators.raters.bobbyrules

import java.text.SimpleDateFormat
import java.time.{Clock, LocalDate}
import java.util.Date

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Rater, Rating}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class BobbyRulesRater @Inject()(
   bobbyRuleConnector: BobbyRuleConnector
   )(implicit val ec: ExecutionContext)
    extends Rater {

    private implicit val hc: HeaderCarrier = HeaderCarrier()
    private val logger = Logger(this.getClass)

    override def rate(repo: String): Future[Rating] = {
        logger.info(s"Rating LeakDetection for: $repo")

        getDependencyList(repo).map(i => countViolationsForRepo(i))
    }

    def getDependencyList(repo: String): Future[Seq[Dependencies]] = {
        for {
            bobbyRuleReport: Option[Report] <- bobbyRuleConnector.findLatestMasterReport(repo)
            dependencies: Seq[Dependencies] = bobbyRuleReport.map(b => {
                b.libraryDependencies ++ b.sbtPluginsDependencies ++ b.otherDependencies
            }).getOrElse(Seq())
        } yield dependencies
    }

    def countViolationsForRepo(dependencies: Seq[Dependencies], now: LocalDate = LocalDate.now): BobbyRulesRating = {
        val dependencyFromDates = dependencies.flatMap(_.bobbyRuleViolations.map(_.from))

        val (pendingViolations, activeViolations) = dependencyFromDates
            .partition(
                _.isAfter(now))

        BobbyRulesRating(pendingViolations.size, activeViolations.size)
    }
}


