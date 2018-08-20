package akkadb

import akka.Done
import akka.actor.Scheduler
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.{LWWMap, LWWMapKey}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

class NewAkkaDBImpl[K, V](dbName: String) extends AkkaDB[K, V] {

  val port = "2552"

  val config = ConfigFactory
    .parseString(
      s"""
        |akka.remote.netty.tcp.port=$port
        |akka.remote.artery.canonical.port=$port
        |""".stripMargin
    )
    .withFallback(ConfigFactory.load())

  // val system: ActorSystem[Nothing]             = ActorSystem(Behaviors.empty, "testHttp", config)
  val system: ActorSystem[Nothing]             = ActorSystem(Behaviors.empty, "testHttp")
  val replicator: ActorRef[Replicator.Command] = DistributedData(system).replicator
  implicit val cluster: Cluster                = akka.cluster.Cluster(system.toUntyped)
  implicit val scheduler: Scheduler            = system.scheduler
  implicit val ec: ExecutionContext            = system.executionContext
  implicit val timeout: Timeout                = Timeout(5.seconds)

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  override def set(key: K, value: V): Future[Done] = {
    val update: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ + (key, value))

    (replicator ? update).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in set")
        Done
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

  override def list: Future[Map[K, V]] = {
    println("Inside list....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = replicator ? get

    result.map {
      case r @ Replicator.GetSuccess(k, v) => {
        val value = r.get(k)
        println("In list success...." + value.entries)
        value.entries
      }
      //Handle exceptions GetFailure and Notfound
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  override def get(key: K): Future[Option[V]] = {
    println("Inside get ....")
    list.map(_.get(key))
  }

  override def remove(key: K): Future[Done] = {
    val remove: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ - key)

    (replicator ? remove).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in remove")
        Done
      case x => throw new RuntimeException(s"deletion failed due to: $x")
    }
  }

}
