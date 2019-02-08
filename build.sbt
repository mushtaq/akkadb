inThisBuild(
  List(
    name := "AkkaStore",
    version := "0.1",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      //"-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Xfuture",
      //"-Xprint:typer"
    )
  )
)

lazy val AkkaVersion     = "2.5.20"
lazy val AkkaHttpVersion = "10.1.7"

lazy val `akka-store-api` = project.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json"                % "2.7.1",
    "com.typesafe.akka" %% "akka-stream-typed"        % AkkaVersion,
    "org.julienrf"      %% "play-json-derived-codecs" % "5.0.0"
  )
)

lazy val `akka-store-common` = project
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka"      %% "akka-actor"        % AkkaVersion,
      "com.typesafe.play"      %% "play-json"         % "2.7.1",
      "com.typesafe.akka"      %% "akka-http"         % AkkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-testkit" % AkkaHttpVersion,
      "org.scala-lang.modules" %% "scala-async"       % "0.9.7",
    )
  )

lazy val `akka-store-client` = project.dependsOn(`akka-store-api`, `akka-store-common`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest"     %% "scalatest"          % "3.0.6-SNAP6" % Test,
    )
  )

lazy val `akka-store-server` = project
  .dependsOn(`akka-store-api`, `akka-store-common`)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed"  % AkkaVersion,
      "org.scalatest"     %% "scalatest"          % "3.0.6-SNAP6" % Test,
    )
  )
