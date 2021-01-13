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

import java.time.LocalDate

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.{Dependencies, Dependency, ServiceDependenciesConnector}
import uk.gov.hmrc.healthindicators.models.{Rater, Rating}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BobbyRulesRater @Inject() (
  serviceDependenciesConnector: ServiceDependenciesConnector
)(implicit val ec: ExecutionContext)
    extends Rater {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val logger                     = Logger(this.getClass)

  override def rate(repo: String): Future[Rating] = {
    logger.info(s"Rating LeakDetection for: $repo")

    getDependencyList(repo).map(i => countViolationsForRepo(i))
  }

  def getDependencyList(repo: String): Future[Seq[Dependency]] =
    for {
      maybeDependencies: Option[Dependencies] <- serviceDependenciesConnector.dependencies(repo)
      dependencies: Seq[Dependency] = maybeDependencies
                                        .map { b =>
                                          b.libraryDependencies ++ b.sbtPluginsDependencies ++ b.otherDependencies
                                        }
                                        .getOrElse(Seq())
    } yield dependencies

  def countViolationsForRepo(dependencies: Seq[Dependency], now: LocalDate = LocalDate.now): BobbyRulesRating = {
    val dependencyFromDates = dependencies.flatMap(_.bobbyRuleViolations.map(_.from))

    val (pendingViolations, activeViolations) = dependencyFromDates
      .partition(_.isAfter(now))

    BobbyRulesRating(pendingViolations.size, activeViolations.size)
  }
}
