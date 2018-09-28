package akkastore.server

import com.typesafe.config._
import akka.http.scaladsl.Http

import scala.concurrent.Future

class AkkaStoreServer(route: AkkaStoreRoutes, actorRuntime: ActorRuntime) {
  import actorRuntime._

  //Http port and host name will be read from application.conf file
  val config          = ConfigFactory.load()
  val httpPserverPort = config.getInt("akka.http-port")
  val httphost        = config.getString("akka.http-host")

  def start(): Future[Http.ServerBinding] = {
    println("Akka http server started at  *** " + httphost + ":" + httpPserverPort)
    Http().bindAndHandle(route.route, httphost, httpPserverPort)
  }
}
