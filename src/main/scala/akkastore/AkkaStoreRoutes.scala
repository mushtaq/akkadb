package akkastore

import akka.Done
import akka.http.scaladsl.server.{Directives, Route}
import akkastore.JsonSupport._

import scala.concurrent.Future

class AkkaStoreRoutes(akkaStore: AkkaStore[String, String]) extends JsonSupport with Directives {

  val route: Route =
    pathPrefix("akkastore") {
      get {
        path("demo-store" / "list") {
          println("* * *In demo-db list * * *")
          val result = akkaStore.list
          //println(s"$result")
          complete(result)
        }
      } ~
      post {
        path("demo-store" / "get") {
          entity(as[AkkaStoreGet]) { akkastoreget =>
            println(s"* * *In demo-db get. Key is - ${ akkastoreget.key} * * *")
            complete(akkaStore.get(akkastoreget.key))
          }
        } ~
        path("demo-store" / "set") {
          entity(as[AkkaStoreSet]) { akkastoreset =>
            println(s"* * * In demo-db set. Key : ${akkastoreset.key} - Value : ${akkastoreset.value} * * *")
            val result: Future[Done] = akkaStore.set(akkastoreset.key, akkastoreset.value)
            onSuccess(result) { _ =>
              complete(s"Successfully added to store - Key : ${akkastoreset.key} - Value : ${akkastoreset.value}")
              //complete(akkastoreset)
            }
          }
        } ~
        path("demo-store" / "remove") {
          entity(as[AkkaStoreRemove]) { akkastoreremove =>
            println(s"* * *In demo-db remove. Key is - ${akkastoreremove.key} * * *")
            val result = akkaStore.remove(akkastoreremove.key)
            onSuccess(result) { _ =>
              complete(s"Successfully removed from store key - ${akkastoreremove.key}")
            }
          }
        }
      }
    }
}
