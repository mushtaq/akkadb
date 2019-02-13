package akkastore.server

import com.typesafe.config._
import akka.http.scaladsl.Http

import scala.concurrent.Future

class AkkaStoreServer(route: AkkaStoreRoutes, actorRuntime: ActorRuntime) {
  import actorRuntime._

  private val config          = ConfigFactory.load()
  private val httpPserverPort = config.getInt("akka.http-port")

  def start(): Future[Http.ServerBinding] = {
    println("Akka http server started at  *** 0.0.0.0:" + httpPserverPort)
    Http().bindAndHandle(route.route, "0.0.0.0", httpPserverPort)
  }
}
