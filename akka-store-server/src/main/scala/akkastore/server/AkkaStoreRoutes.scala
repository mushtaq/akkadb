package akkastore.server

import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akkastore.api._
import akkastore.common.PlayJsonSupport
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class AkkaStoreRoutes(actorRuntime: ActorRuntime) extends JsonSupport with PlayJsonSupport with Directives {
  import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

  val route: Route =
    pathPrefix("akkastore" / Segment) { dbName =>
      val jsonAkkaStore: AkkaStore[JsValue, JsValue] = new AkkaStoreImpl(dbName, actorRuntime)

      get {
        path("list") {
          println(s"* * *In $dbName list * * *")
          complete(jsonAkkaStore.list)
        }
      } ~
      post {
        path("get") {
          entity(as[JsValue]) { key =>
            println(s"* * *In $dbName get. Key is - $key * * *")
            complete(jsonAkkaStore.get(key))
          }
        } ~
        path("set") {
          entity(as[KVPayload[JsValue, JsValue]]) {
            case KVPayload(key, value) =>
              println(s"* * * In $dbName set. Key : $key * * *")
              onSuccess(jsonAkkaStore.set(key, value)) { _ =>
                complete(s"Successfully set value for key=$key")
              }
          }
        } ~
        path("remove") {
          entity(as[JsValue]) { key =>
            println(s"* * *In $dbName remove. Key is - $key * * *")
            onSuccess(jsonAkkaStore.remove(key)) { _ =>
              complete(s"Successfully removed key=$key")
            }
          }
        } ~
        path("watch") {
          entity(as[JsValue]) { key =>
            println(s"* * *In $dbName watch. Key is - $key * * *")
            complete {
              jsonAkkaStore
                .watch(key)
                .map { msg =>
                  ServerSentEvent(Json.toJson(msg).toString)
                }
                .keepAlive(1.second, () => ServerSentEvent.heartbeat)
            }
          }
        }
      }
    }
}
