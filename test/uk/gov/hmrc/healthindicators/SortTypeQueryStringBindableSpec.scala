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

package uk.gov.hmrc.healthindicators

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.SortType

class SortTypeQueryStringBindableSpec extends AnyWordSpec with Matchers with OptionValues {

  "A SortType QueryString Parameter" should {

    "be bound to a SortType value when valid" in {
      SortType.values.foreach { sortType =>
        val params = Map("sort" -> Seq(sortType.asString))

        SortType.sortTypeBindable.bind(key = "sort", params).value shouldBe Right(sortType)
      }
    }

    "not be bound when value is not found" in {
      SortType.sortTypeBindable.bind(key = "sort", Map.empty) shouldBe None
    }

    "fail to bind when there is no matching value" in {
      val params = Map("sort" -> Seq.empty)

      SortType.sortTypeBindable.bind(key = "sort", params).value.isLeft shouldBe true

      // Alternative statement without OptionValues???
      //SortType.sortTypeBindable.bind(key = "sort", params).get.isLeft shouldBe true
    }

    "fail to bind when value is not recognised" in {
      val params = Map("sort" -> Seq("unknown"))

      SortType.sortTypeBindable.bind(key = "sort", params).value.isLeft shouldBe true
    }

    "fail to bind when there is more than one recognised value" in {
      val params = Map("sort" -> Seq(SortType.Descending.asString, SortType.Ascending.asString))

      SortType.sortTypeBindable.bind(key = "sort", params).value.isLeft shouldBe true
    }

  }

}
