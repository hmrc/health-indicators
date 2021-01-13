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

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class ScoreConfig {
  val bobbyRuleActive  = -100
  val bobbyRulePending = -20
  val leakDetection    = -50
  val validReadMe      = 50
  val noReadMe         = -50
  val defaultReadMe    = -50
}
