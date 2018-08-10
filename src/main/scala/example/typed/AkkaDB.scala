package example.typed

import TestAkkaDB.{AkkaDbMsg, ResponseGet}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata._
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.ReadLocal
import akka.cluster.ddata.protobuf.msg.ReplicatedDataMessages

object AkkaDB {

  sealed trait ActionOnDB
  final case class Set(key: String, value: Int)                   extends ActionOnDB
  final case class GetAll(replyTo: ActorRef[AkkaDbMsg])           extends ActionOnDB
  final case class Get(key: String, replyTo: ActorRef[AkkaDbMsg]) extends ActionOnDB
  final case class Remove(key: String)                            extends ActionOnDB

  private sealed trait InternalMsg                                                                  extends ActionOnDB
  private case class InternalUpdateResponse[A <: ReplicatedData](rsp: Replicator.UpdateResponse[A]) extends InternalMsg
  private case class InternalGetResponse[A <: ReplicatedData](rsp: Replicator.GetResponse[A])       extends InternalMsg

  val bhvrAkkaDD: Behavior[AkkaDB.ActionOnDB] = Behaviors.receive { (ctx, msg) ⇒
    implicit val cluster: Cluster                = akka.cluster.Cluster(ctx.system.toUntyped)
    val replicator: ActorRef[Replicator.Command] = DistributedData(ctx.system).replicator

    //This key should hv some additional variable added to it like say table name that comes from outside this object
    val DataKey = LWWMapKey[String, Int]("LWWMapKey")

    val getResponseAdapter: ActorRef[Replicator.GetResponse[LWWMap[String, Int]]] =
      ctx.messageAdapter(InternalGetResponse.apply)

    val updateResponseAdapter: ActorRef[Replicator.UpdateResponse[LWWMap[String, Int]]] =
      ctx.messageAdapter(InternalUpdateResponse.apply)

    msg match {

      case Set(key, val1) =>
        println("Adding key and value *** ")
        val update =
          Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal, updateResponseAdapter)(_ + (key, val1))
        replicator ! update
        println("AfterUpdate *** ")
        Behaviors.same

      case Remove(key) =>
        println("before removing ***")
        val remove =
          Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal, updateResponseAdapter)(_ - (key))
        replicator ! remove
        Behaviors.same

      case Get(key, replyTo) =>
        //replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo))
        replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo, Some(key)))
        Behaviors.same

      case GetAll(replyTo) =>
        replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo, None))
        Behaviors.same

      case internal: InternalMsg ⇒
        internal match {
          case InternalUpdateResponse(_) ⇒ Behaviors.same // ok

          case InternalGetResponse(
              rsp @ Replicator.GetSuccess(DataKey,
                                          Some((replyTo: ActorRef[AkkaDbMsg] @unchecked, key: Option[String] @unchecked)))
              ) ⇒
            val values = rsp.get(DataKey)
            // You will get this message in case of GetAll and Get both.
            // In case of Get we also want to pass key from Get to here to extract precise value
            val entries: Map[String, Int] = key match {
              case Some(k) => Map(k -> values.entries(k))
              case _       => values.entries

            }
            //println("Current Elements ***: {}" + values.entries.toSet.toString())
            replyTo ! ResponseGet(entries)
            Behaviors.same

          case InternalGetResponse(rsp) ⇒
            Behaviors.unhandled // not dealing with failures
            Behaviors.same
        }

      case _ =>
        println(msg.toString + "Duh...") //Some other msg than DB operations
        Behaviors.same

    }
  }

}
