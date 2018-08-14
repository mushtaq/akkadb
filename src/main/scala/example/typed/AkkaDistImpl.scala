package example.typed
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.ddata.typed.scaladsl.Replicator.Update
import akka.cluster.ddata.{LWWMap, LWWMapKey, Replicator}
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import example.typed.AkkaDB.ActionOnDB
import example.typed.AkkaDistDB

import scala.concurrent.Future

class AkkaDistImpl(system: ActorSystem[ActionOnDB]) extends AkkaDistDB {

  val replicator                = DistributedData(system).replicator
  implicit val cluster: Cluster = akka.cluster.Cluster(system.toUntyped)

  //This key should hv some additional variable added to it like say table name that comes from outside this object
  val DataKey = LWWMapKey[String, Int]("LWWMapKey")

  def set(key: String, value: Int) = {
    val update = akka.cluster.ddata.Replicator
      .Update(DataKey, LWWMap.empty[String, Int], akka.cluster.ddata.Replicator.WriteLocal)((_ + (key, value)))
    //replicator ! update
  }

  def getAll(): Future[Map[String, Int]] = {
    //val getValues: Map[String, Int] = Map("a"-> 1)
  }

  def get(key: String): Future[Map[String, Int]] = {}
  def remove(key: String)                        = {}

}
