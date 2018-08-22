package akkadb

import akka.Done
import akka.http.scaladsl.server.{Directives, Route}
import akkadb.JsonSupport._

import scala.concurrent.Future

class AkkaDbRoutes(akkaDb: AkkaDB[String, String]) extends JsonSupport with Directives {

  val route: Route =
    pathPrefix("akkadb") {
      get {
        path("demo-db" / "list") {
          println("* * *In demo-db list * * *")
          complete(akkaDb.list)
        }
      } ~
      post {
        path("demo-db" / "get") {
          entity(as[AkkadbGet]) { akkadbget =>
            println(s"* * *In demo-db get. Key is - ${akkadbget.key} * * *")
            //println(akkadbget.key)
            complete(akkaDb.get(akkadbget.key))
          }
        } ~
        path("demo-db" / "set") {
          entity(as[AkkadbSet]) { akkadbset =>
            println(s"* * * In demo-db set. Key : ${akkadbset.key} - Value : ${akkadbset.value} * * *")
            // println(s"${akkadbset.key}")
            //println(s"${akkadbset.value}")
            val result: Future[Done] = akkaDb.set(akkadbset.key, akkadbset.value)
            onSuccess(result) { _ =>
              complete(s"Successfully added to store - Key : ${akkadbset.key} - Value : ${akkadbset.value}")
            }
          }
        } ~
        path("demo-db" / "remove") {
          entity(as[AkkadbRemove]) { akkadbremove =>
            println(s"* * *In demo-db remove. Key is - ${akkadbremove.key} * * *")
            //println(s"${akkadbremove.key}")
            val result = akkaDb.remove(akkadbremove.key)
            onSuccess(result) { _ =>
              complete(s"Successfully removed from store key - ${akkadbremove.key}")
            }
          }
        }
      }
    }
}
