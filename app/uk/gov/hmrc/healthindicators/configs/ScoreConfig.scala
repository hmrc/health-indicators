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

package uk.gov.hmrc.healthindicators.configs

import javax.inject.Singleton
import uk.gov.hmrc.healthindicators.models.{BobbyRuleActive, BobbyRuleIndicatorType, BobbyRulePending, DefaultReadme, IndicatorType, LeakDetectionIndicatorType, LeakDetectionViolation, NoReadme, ReadMeIndicatorType, ValidReadme}

@Singleton
class ScoreConfig {

  def scores(indicatorType: IndicatorType): Int = {
    indicatorType match {
      case i: ReadMeIndicatorType => i match {
        case NoReadme => -50
        case DefaultReadme => -50
        case ValidReadme => 50
      }
      case i: LeakDetectionIndicatorType => i match {
        case LeakDetectionViolation => -50
      }
      case i: BobbyRuleIndicatorType => i match {
        case BobbyRulePending => -20
        case BobbyRuleActive => -100
      }
    }
  }
}

