package akkastore.server

import akka.actor
import akka.actor.Scheduler
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ActorRuntime(system: ActorSystem[Nothing]) {
  val replicator: ActorRef[Replicator.Command]  = DistributedData(system).replicator
  implicit val untypedSystem: actor.ActorSystem = system.toUntyped
  implicit val cluster: Cluster                 = akka.cluster.Cluster(untypedSystem)
  implicit val scheduler: Scheduler             = system.scheduler
  implicit val ec: ExecutionContext             = system.executionContext
  implicit val mat: Materializer                = ActorMaterializer()
  implicit val timeout: Timeout                 = Timeout(5.seconds)
}
