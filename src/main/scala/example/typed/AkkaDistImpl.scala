package example.typed

import akka.Done
import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.{LWWMap, LWWMapKey}
import akka.util.Timeout
import example.typed.AkkaDB.ActionOnDB

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class AkkaDistImpl(system: ActorSystem[ActionOnDB]) extends AkkaDistDB {

  private val replicator                    = DistributedData(system).replicator
  private implicit val cluster: Cluster     = akka.cluster.Cluster(system.toUntyped)
  private implicit val scheduler: Scheduler = system.scheduler
  import system.executionContext
  private implicit val timeout: Timeout = Timeout(5.seconds)

  //This key should hv some additional variable added to it like say table name that comes from outside this object
  val DataKey: LWWMapKey[String, Int] = LWWMapKey[String, Int]("LWWMapKey")

  override def set(key: String, value: Int): Future[Done] = {
    val update: ActorRef[UpdateResponse[LWWMap[String, Int]]] => Update[LWWMap[String, Int]] =
      Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal)(_ + (key, value))

    (replicator ? update).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in set")
        Done
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

  override def list: Future[Map[String, Int]] = {
    println("Inside getAll....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = replicator ? get

    result.map {
      case r @ Replicator.GetSuccess(k, v) => {
        val value = r.get(k)
        println("In Get Sucess...." + value.entries)
        value.entries
      }
      //Handle exceptions GetFailure and Notfound
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  override def get(key: String): Future[Option[Int]] = {
    println("Inside get ....")
    list.map(_.get(key))
  }

  override def remove(key: String): Future[Done] = {
    val remove: ActorRef[UpdateResponse[LWWMap[String, Int]]] => Update[LWWMap[String, Int]] =
      Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal)(_ - key)

    (replicator ? remove).map {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in remove")
        Done
      case x => throw new RuntimeException(s"deletion failed due to: $x")
    }
  }

}
