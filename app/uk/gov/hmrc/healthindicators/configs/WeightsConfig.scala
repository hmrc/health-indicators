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

package uk.gov.hmrc.healthindicators.configs

import cats.Applicative
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.models.RatingType

@Singleton
class WeightsConfig @Inject()(configuration: Configuration) {

  import WeightsConfig._

  lazy val weightsLookup: Map[RatingType, Double] = {
    implicit val wF = reads
    val path        = configuration.get[String]("weights.config.path")
    readJson(path)
      .validate[Map[RatingType, Double]]
      .recoverTotal(e => sys.error(s"Invalid Json when reading from $path: $e"))
  }
}

object WeightsConfig {

  def readJson(path: String): JsValue = {
    val stream = getClass.getResourceAsStream(path)

    try {
      Json.parse(stream)
    } finally {
      stream.close()
    }
  }

  val reads: Reads[Map[RatingType, Double]] = {
    implicit val rtF = RatingType.format

    implicit val rtA: Applicative[JsResult] = new Applicative[JsResult] {
      override def pure[A](x: A): JsResult[A] = JsSuccess(x)

      override def ap[A, B](ff: JsResult[A => B])(fa: JsResult[A]): JsResult[B] =
        (ff, fa) match {
          case (JsSuccess(f, p), JsSuccess(a, _)) => JsSuccess(f(a), p)
          case (e: JsError, _)                    => e
          case (_, e: JsError)                    => e
        }
    }

    Reads
      .of[Map[String, Double]]
      .flatMap(
        m =>
          Reads(_ =>
            m.toList
              .traverse {
                case (k, v) => JsString(k).validate[RatingType].map((_, v))
              }
              .map(_.toMap)))
  }
}
