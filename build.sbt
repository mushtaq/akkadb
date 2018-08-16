name := "AkkaDB"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akkaTypedVersion = "2.5.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"      % akkaTypedVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"    % akkaTypedVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaTypedVersion,

  "org.scalatest" %% "scalatest" % "3.0.5" % "test"

)
