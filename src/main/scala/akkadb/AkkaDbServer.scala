package akkadb
import akka.http.scaladsl.Http

import scala.concurrent.Future

class AkkaDbServer(route: AkkaDbRoutes, actorRuntime: ActorRuntime) {
  import actorRuntime._
  def start(): Future[Http.ServerBinding] = {
    println("server started don't know the port *** ")
    Http().bindAndHandle(route.route, "localhost", 8080)
  }
}
