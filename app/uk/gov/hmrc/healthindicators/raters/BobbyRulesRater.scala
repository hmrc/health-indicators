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

package uk.gov.hmrc.healthindicators.raters

import java.time.LocalDate

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.ServiceDependenciesConnector
import uk.gov.hmrc.healthindicators.models.{BobbyRuleActive, BobbyRuleIndicator, Indicator, BobbyRulePending}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BobbyRulesRater @Inject()(
                                 serviceDependenciesConnector: ServiceDependenciesConnector
                               )(implicit val ec: ExecutionContext)
  extends Rater {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val logger = Logger(this.getClass)

  override def rate(repo: String): Future[Seq[Indicator]] = {
    logger.info(s"Rating LeakDetection for: $repo")
    val now = LocalDate.now

    serviceDependenciesConnector.dependencies(repo).map { maybeDependencies =>

      for {
        dependencies <- maybeDependencies.toSeq
        dependency <- dependencies.libraryDependencies ++ dependencies.sbtPluginsDependencies ++ dependencies.otherDependencies
        violation <- dependency.bobbyRuleViolations
        result = if (violation.from.isBefore(now)) {
          BobbyRuleActive
        } else {
          BobbyRulePending
        }
      } yield Indicator(BobbyRuleIndicator(result), s"${dependency.name} - ${violation.reason}", None)
    }
  }
}
