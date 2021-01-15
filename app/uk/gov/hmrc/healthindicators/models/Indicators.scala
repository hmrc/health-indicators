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

sealed trait ReadMeIndicatorType extends IndicatorType
case object NoReadme extends ReadMeIndicatorType
case object DefaultReadme extends ReadMeIndicatorType
case object ValidReadme extends ReadMeIndicatorType

sealed trait LeakDetectionIndicatorType extends IndicatorType
case object LeakDetectionViolation extends LeakDetectionIndicatorType

sealed trait BobbyRuleIndicatorType extends IndicatorType
case object BobbyRulePending extends BobbyRuleIndicatorType
case object BobbyRuleActive extends BobbyRuleIndicatorType

sealed trait Result {
  def value(): IndicatorType
}
case class ReadMeIndicator(value: ReadMeIndicatorType) extends Result
case class LeakDetectionIndicator(value: LeakDetectionIndicatorType) extends Result
case class BobbyRuleIndicator(value: BobbyRuleIndicatorType) extends Result

case class x (ratingType: RatingType, indicatorType: IndicatorType )
case class Indicator(
                      result: Result,
                      description: String,
                      href: Option[String]
                    )

case class ServiceHealthIndicator(
                                   repositoryName: String,
                                   timestamp: Instant,
                                   indicators: Seq[Indicator]
                                 )
