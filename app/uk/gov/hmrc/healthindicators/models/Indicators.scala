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

package uk.gov.hmrc.healthindicators.models

import java.time.Instant

sealed trait IndicatorType
case object ReadMeIndicator extends IndicatorType
case object LeakDetectionIndicator extends IndicatorType
case object BobbyRuleIndicator extends IndicatorType

case class ServiceHealthIndicator(
                                   repositoryName: String,
                                   timestamp: Instant,
                                   indicators: Seq[Indicator])

case class Indicator(indicatorType: IndicatorType, results: Seq[Result])

case class Result(
                   description: String,
                   href: Option[String],
                   resultType: ResultType
                 )

sealed trait ResultType
sealed trait ReadMeResultType extends ResultType

case object NoReadme extends ReadMeResultType
case object DefaultReadme extends ReadMeResultType
case object ValidReadme extends ReadMeResultType

sealed trait LeakDetectionResultType extends ResultType
case object LeakDetection extends LeakDetectionResultType

sealed trait BobbyRuleResultType extends ResultType
case object BobbyRulePending extends BobbyRuleResultType
case object BobbyRuleActive extends BobbyRuleResultType
