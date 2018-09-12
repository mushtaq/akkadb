package akkastore.server

import akka.http.scaladsl.server.{Directives, Route}
import akkastore.api.{AkkaStore, JsonSupport, KVPayload}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.JsValue

class AkkaStoreRoutes(actorRuntime: ActorRuntime) extends JsonSupport with PlayJsonSupport with Directives {

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
              println(s"* * * In $dbName set. Key : $key - Value : $value * * *")
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
        }
      }
    }
}
