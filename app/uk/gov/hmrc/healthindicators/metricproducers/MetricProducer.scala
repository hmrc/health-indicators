package uk.gov.hmrc.healthindicators.metricproducers

import uk.gov.hmrc.healthindicators.models.Metric

import scala.concurrent.Future

trait MetricProducer {
  def produce(repo: String): Future[Metric]
}
