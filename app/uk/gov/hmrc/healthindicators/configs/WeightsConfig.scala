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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json

@Singleton
class WeightsConfig @Inject()(configuration: Configuration) {

  lazy val weightsLookup: Map[String, Double] = {
    val weightsConfigFilePath = configuration.get[String]("weights.config.path")

    val stream = getClass.getResourceAsStream(weightsConfigFilePath)
    val json =
      try {
        Json.parse(stream)
      } finally {
        stream.close()
      }
    json.as[Map[String, Double]]
  }
}
