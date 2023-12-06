import play.sbt.routes.RoutesKeys

val appName = "health-indicators"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.12",
    libraryDependencies     ++= AppDependencies.compile ++ AppDependencies.test,
    RoutesKeys.routesImport ++= Seq(
      "uk.gov.hmrc.healthindicators.models.SortType",
      "uk.gov.hmrc.healthindicators.connectors.RepoType"
    ),
    PlayKeys.playDefaultPort := 9018
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
