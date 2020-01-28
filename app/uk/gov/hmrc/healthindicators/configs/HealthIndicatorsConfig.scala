package uk.gov.hmrc.healthindicators.configs

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.healthindicators.utils.ConfigUtils
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class HealthIndicatorsConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) extends ConfigUtils {

  lazy val teamsAndRepositoriesUrl: String = servicesConfig.baseUrl("teams-and-repositories")
}
