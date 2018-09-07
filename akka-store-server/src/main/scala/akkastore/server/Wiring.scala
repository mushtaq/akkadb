package akkastore.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akkastore.api.AkkaStore
import play.api.libs.json.JsValue

class Wiring {
  lazy val system                                = ActorSystem(Behaviors.empty, "akka-store")
  lazy val actorRuntime                          = new ActorRuntime(system)
  lazy val akkStore: AkkaStore[JsValue, JsValue] = new AkkaStoreImpl("demo-store", actorRuntime)
  lazy val akkaStoreRoutes                       = new AkkaStoreRoutes(akkStore)
  lazy val akkaStoreServer                       = new AkkaStoreServer(akkaStoreRoutes, actorRuntime)
}
