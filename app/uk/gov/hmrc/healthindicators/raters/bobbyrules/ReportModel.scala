/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class BobbyRuleViolations (
   reason: String,
   from: String,
   range: String
)

object BobbyRuleViolations {
    val reads: Reads[BobbyRuleViolations] = {
        ((__ \ "reason").read[String]
            ~ (__ \ "from").read[String]
            ~ (__ \ "range").read[String])(BobbyRuleViolations.apply _)
    }
}

case class Dependencies (
   bobbyRuleViolations: Seq[BobbyRuleViolations],
   name: String
)

object Dependencies {
    val reads: Reads[Dependencies] = {
        implicit val brvR = BobbyRuleViolations.reads
        ((__ \ "bobbyRuleViolations").read[Seq[BobbyRuleViolations]]
            ~ (__ \ "name").read[String])(Dependencies.apply _)
    }
}

case class Report (
     repositoryName: String,
     libraryDependencies: Seq[Dependencies],
     sbtPluginsDependencies: Seq[Dependencies],
     otherDependencies: Seq[Dependencies],
 )

object Report {
    val reads: Reads[Report] = {
        implicit val ldR = Dependencies.reads
        ((__ \ "repositoryName").read[String]
            ~ (__ \ "libraryDependencies").read[Seq[Dependencies]]
            ~ (__ \ "sbtPluginsDependencies").read[Seq[Dependencies]]
            ~ (__ \ "otherDependencies").read[Seq[Dependencies]])(Report.apply _)
    }
}

