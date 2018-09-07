package akkastore.server

import akka.http.scaladsl.server.{Directives, Route}
import akkastore.api.{AkkaStore, JsonSupport, KVPayload}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.JsValue

class AkkaStoreRoutes(akkaStore: AkkaStore[JsValue, JsValue]) extends JsonSupport with PlayJsonSupport with Directives {

  val route: Route =
    pathPrefix("akkastore") {
      get {
        path("demo-store" / "list") {
          println("* * *In demo-db list * * *")
          complete(akkaStore.list)
        }
      } ~
      post {
        path("demo-store" / "get") {
          entity(as[JsValue]) { key =>
            println(s"* * *In demo-db get. Key is - $key * * *")
            complete(akkaStore.get(key))
          }
        } ~
        path("demo-store" / "set") {
          entity(as[KVPayload[JsValue, JsValue]]) {
            case KVPayload(key, value) =>
              println(s"* * * In demo-db set. Key : $key - Value : $value * * *")
              onSuccess(akkaStore.set(key, value)) { result =>
                println(result)
                complete(s"Successfully set value for key=$key")
              }
          }
        } ~
        path("demo-store" / "remove") {
          entity(as[JsValue]) { key =>
            println(s"* * *In demo-db remove. Key is - $key * * *")
            onSuccess(akkaStore.remove(key)) { _ =>
              complete(s"Successfully removed key=$key")
            }
          }
        }
      }
    }
}
