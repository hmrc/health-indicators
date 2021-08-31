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

package uk.gov.hmrc.healthindicators.services

import uk.gov.hmrc.healthindicators.models.{AveragePlatformScore, HistoricIndicator, HistoricIndicatorAPI, SortType}
import uk.gov.hmrc.healthindicators.persistence.{AveragePlatformScoreRepository, HistoricIndicatorsRepository}

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HistoricIndicatorService @Inject() (
  repoIndicatorService: RepoIndicatorService,
  historicIndicatorsRepository: HistoricIndicatorsRepository,
  averagePlatformScoreRepository: AveragePlatformScoreRepository
)(implicit ec: ExecutionContext) {

  def collectHistoricIndicators(): Future[Unit] = {
    val now = Instant.now()
    for {
      allRepos <- repoIndicatorService.indicatorsForAllRepos(None, SortType.Ascending)
      historicIndicators = allRepos.map(x => HistoricIndicator(x.repoName, now, x.overallScore))
      average            = historicIndicators.foldLeft(0)((total, i) => i.overallScore + total) / historicIndicators.length
      platformAverage    = AveragePlatformScore(now, average)
      _      <- historicIndicatorsRepository.insert(historicIndicators)
      result <- averagePlatformScoreRepository.insert(platformAverage)
    } yield result
  }

  def historicIndicatorForRepo(repoName: String): Future[Option[HistoricIndicatorAPI]] =
    historicIndicatorsRepository
      .findAllForRepo(repoName)
      .map(i => HistoricIndicatorAPI.fromHistoricIndicators(i))

  def historicIndicatorsForAllRepos: Future[Seq[HistoricIndicatorAPI]] =
    historicIndicatorsRepository.findAll.map(all =>
      all
        .groupBy(_.repoName)
        .values
        .flatMap(HistoricIndicatorAPI.fromHistoricIndicators)
        .toSeq
    )
}
