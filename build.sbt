name := "AkkaDB"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaTypedVersion = "2.5.14"
lazy val akkaHttpVersion = "10.1.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"      % akkaTypedVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"    % akkaTypedVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaTypedVersion,

  "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaTypedVersion
)
