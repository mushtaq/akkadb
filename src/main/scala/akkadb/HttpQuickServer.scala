package akkadb

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.ActorMaterializer
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//Quick testing done with plain text by sending following to the server
//http POST localhost:2551/demo-db
//http GET localhost:2551/demo-db

//object HttpQuickServer extends App with Directives {
class HttpQuickServer extends App with Directives {

  val system              = ActorSystem(Behaviors.empty, "testHttp")
  val runtime             = new ActorRuntime(system)
  implicit val unTypedSys = system.toUntyped

  implicit val actorMaterializer = ActorMaterializer()(unTypedSys)

  val objStr: AkkaDB[String, String] = new AkkaDBImpl("demo-db", runtime)

  Thread.sleep(3000)

  val route =
  post {
    path("demo-db") {
      //entity(as[AkkadbSet]) { akkadbset =>
      println("AkkaDB POST")
      //akkaDb.set("a", "val1")
      complete("from set")
    }
  } ~
  get {
    path("demo-db")
    println("in AkkaDB GET")
    complete {
      //akkaDb.set("a", "val1")
      "Hello back from akka HTTP in GET"
    }
  }

  //create server here and bind to this route -- Here our Actor System is already on 2551,
  //need to bind by finding the exisitng host and port name where actor system is already started using application.conf settings
  Http().bindAndHandle(route, "localhost", 2551)
  println("server started don't know the port *** ")

  //Await.result(system.whenTerminated, Duration.Inf)
}
