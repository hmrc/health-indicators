import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("health-indicators", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies     ++= AppDependencies.compile ++ AppDependencies.test,
    RoutesKeys.routesImport ++= Seq(
      "uk.gov.hmrc.healthindicators.models.SortType",
      "uk.gov.hmrc.healthindicators.connectors.RepoType"
    ),
    PlayKeys.playDefaultPort := 9018
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
