package akkadb
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

class Wiring {
  lazy val system                        = ActorSystem(Behaviors.empty, "akka-store")
  lazy val actorRuntime                  = new ActorRuntime(system)
  lazy val akkDb: AkkaDB[String, String] = new AkkaDBImpl[String, String]("demo-db", actorRuntime)
  lazy val akkaDBRoutes                  = new AkkaDbRoutes(akkDb)
  lazy val akkaDbServer                  = new AkkaDbServer(akkaDBRoutes, actorRuntime)
}
