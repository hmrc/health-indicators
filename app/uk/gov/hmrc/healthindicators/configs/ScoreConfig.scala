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

import uk.gov.hmrc.healthindicators.models._

import javax.inject.Singleton

@Singleton
class ScoreConfig {
  def scores(resultType: ResultType): Int =
    resultType match {
      case o: OpenPRResultType =>
        o match {
          case PRsNotFound => 0
          case NoStalePRs  => 0
          case StalePR     => -20
        }
      case r: ReadMeResultType =>
        r match {
          case NoReadme      => -50
          case DefaultReadme => -50
          case ValidReadme   =>  50
        }
      case l: LeakDetectionResultType =>
        l match {
          case LeakDetectionViolation => -50
        }
      case b: BobbyRuleResultType =>
        b match {
          case BobbyRulePending =>  -20
          case BobbyRuleActive  => -100
        }
      case j: JenkinsResultType =>
        j match {
          case JenkinsBuildStable   =>  50
          case JenkinsBuildUnstable => -50
          case JenkinsBuildNotFound =>   0
          case JenkinsBuildOutdated => -50
        }
      case a: AlertConfigResultType =>
        a match {
          case AlertConfigEnabled  => 50
          case AlertConfigDisabled => 20
          case AlertConfigNotFound =>  0
        }
    }
}
