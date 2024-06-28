/*
 * Copyright 2023 HM Revenue & Customs
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
class PointsConfig {
  def points(resultType: ResultType): Int =
    resultType match {
      case g: GithubResultType =>
        g match {
          case StalePR       => -5
          case NoReadme      => -10
          case DefaultReadme => -10
          case CleanGithub   => 10
        }
      case l: LeakDetectionResultType =>
        l match {
          case LeakDetectionViolation => -15
          case LeakDetectionNotFound  => 0
        }
      case a: AlertConfigResultType =>
        a match {
          case AlertConfigEnabled  => 20
          case AlertConfigDisabled => 20
          case AlertConfigNotFound => 0
        }
    }
}
