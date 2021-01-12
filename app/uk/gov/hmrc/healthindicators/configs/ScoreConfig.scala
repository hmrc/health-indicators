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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.Applicative
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.models.Score
import uk.gov.hmrc.healthindicators.raters.bobbyrules.{BobbyRuleViolations, Dependencies, Report}
import play.api.libs.functional.syntax._
import uk.gov.hmrc.healthindicators.configs.ScoreConfig.readJson
import uk.gov.hmrc.healthindicators.raters.bobbyrules.BobbyRulesRating.getClass


@Singleton
class ScoreConfig @Inject()(configuration: Configuration){
 import ScoreConfig._

  lazy val scoreLookup: Score = {
    val path = configuration.get[String]("rating-score.config.path")
    implicit val rS: Reads[Score] = Score.reads
    readJson(path)
      .validate[Score]
      .recoverTotal(e => sys.error(s"Invalid Json when reading from $path: $e"))
  }

}

object ScoreConfig {

  def readJson(path: String): JsValue = {
    val stream = getClass.getResourceAsStream(path)

    try {
      Json.parse(stream)
    } finally {
      stream.close()
    }

  }
}

//implicit val wF: Reads[Map[RatingType, Double]] = reads
//val path        = configuration.get[String]("weights.config.path")
//readJson(path)
//.validate[Map[RatingType, Double]]
//.recoverTotal(e => sys.error(s"Invalid Json when reading from $path: $e"))
//}
//}

//object Report {
//  val reads: Reads[Report] = {
//    implicit val ldR = Dependencies.reads
//    ((__ \ "repositoryName").read[String]
//      ~ (__ \ "libraryDependencies").read[Seq[Dependencies]]
//      ~ (__ \ "sbtPluginsDependencies").read[Seq[Dependencies]]
//      ~ (__ \ "otherDependencies").read[Seq[Dependencies]])(Report.apply _)
//  }
//}









//  import ScoreConfig._
//
//  lazy val ScoreLookup: Map[RatingType, Double] = {
//    implicit val wF: Reads[Map[RatingType, Double]] = reads
//    val path        = configuration.get[String]("score.config.path")
//    readJson(path)
//      .validate[Map[RatingType, Double]]
//      .recoverTotal(e => sys.error(s"Invalid Json when reading from $path: $e"))
//  }
//}
//
//object ScoreConfig {
//
//  def readJson(path: String): JsValue = {
//    val stream = getClass.getResourceAsStream(path)
//
//    try {
//      Json.parse(stream)
//    } finally {
//      stream.close()
//    }
//  }
//
//  //TODO Delete this and understand what is going on. Hard code weights for now
//  val reads: Reads[Map[RatingType, Double]] = {
//    implicit val rtF: Format[RatingType] = RatingType.format
//
//    implicit val rtA: Applicative[JsResult] = new Applicative[JsResult] {
//      override def pure[A](x: A): JsResult[A] = JsSuccess(x)
//
//      override def ap[A, B](ff: JsResult[A => B])(fa: JsResult[A]): JsResult[B] =
//        (ff, fa) match {
//          case (JsSuccess(f, p), JsSuccess(a, _)) => JsSuccess(f(a), p)
//          case (e: JsError, _)                    => e
//          case (_, e: JsError)                    => e
//        }
//    }
//
//    Reads
//      .of[Map[String, Double]]
//      .flatMap(
//        m =>
//          Reads(_ =>
//            m.toList
//              .traverse {
//                case (k, v) => JsString(k).validate[RatingType].map((_, v))
//              }
//              .map(_.toMap)))
//  }
//}
