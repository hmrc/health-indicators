import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.sbt.routes.RoutesKeys.routesImport

val appName = "health-indicators"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion                     := 0,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    RoutesKeys.routesImport ++= Seq(
      "uk.gov.hmrc.healthindicators.models.SortType"
    )
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9018)
  .settings(scalaVersion := "2.13.8")
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")

routesImport += "uk.gov.hmrc.healthindicators.connectors.RepoType"
