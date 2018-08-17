package akkadb

import akka.actor.Scheduler
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ActorRuntime(system: ActorSystem[Nothing]) {
  val replicator: ActorRef[Replicator.Command] = DistributedData(system).replicator
  implicit val cluster: Cluster                = akka.cluster.Cluster(system.toUntyped)
  implicit val scheduler: Scheduler            = system.scheduler
  implicit val ec: ExecutionContext            = system.executionContext
  implicit val timeout: Timeout                = Timeout(5.seconds)
}
