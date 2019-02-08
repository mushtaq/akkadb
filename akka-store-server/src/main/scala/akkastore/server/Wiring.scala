package akkastore.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

class Wiring {
  lazy val system          = ActorSystem(Behaviors.empty, "akka-store")
  lazy val actorRuntime    = new ActorRuntime(system)
  lazy val akkaStoreRoutes = new AkkaStoreRoutes(actorRuntime)
  lazy val akkaStoreServer = new AkkaStoreServer(akkaStoreRoutes, actorRuntime)
}
