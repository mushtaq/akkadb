inThisBuild(
  List(
    name := "AkkaStore",
    version := "0.1",
    scalaVersion := "2.12.6"
  )
)

lazy val AkkaVersion = "2.5.14"
lazy val AkkaHttpVersion  = "10.1.3"

lazy val `akka-store-api` = project.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json"   % "2.6.10",
    "com.typesafe.akka" %% "akka-stream-typed" % "2.5.16",
    "org.julienrf" %% "play-json-derived-codecs" % "4.0.1"
  )
)

lazy val `akka-store-client` = project
  .dependsOn(`akka-store-api`)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-http"           % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-testkit"   % AkkaHttpVersion,
      "org.scalatest"          %% "scalatest"           % "3.0.5" % "test",
      "de.heikoseeberger"      %% "akka-http-play-json" % "1.21.0",
      "org.scala-lang.modules" %% "scala-async"         % "0.9.7",
    )
  )

lazy val `akka-store-server` = project
  .dependsOn(`akka-store-api`)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-actor-typed"      % AkkaVersion,
      "com.typesafe.akka"      %% "akka-cluster-typed"    % AkkaVersion,
      "com.typesafe.akka"      %% "akka-distributed-data" % AkkaVersion,
      "com.typesafe.akka"      %% "akka-http"             % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-xml"         % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-testkit"     % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-stream-typed"     %      "2.5.16",
      "org.scalatest"          %% "scalatest"             % "3.0.5" % "test",
      "de.heikoseeberger"      %% "akka-http-play-json"   % "1.21.0",
      "org.scala-lang.modules" %% "scala-async"           % "0.9.7",
    )
  )
