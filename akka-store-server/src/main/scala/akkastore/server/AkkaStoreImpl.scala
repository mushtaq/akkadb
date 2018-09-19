package akkastore.server

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.cluster.ddata.{LWWMap, LWWMapKey, LWWRegister, LWWRegisterKey, ORSetKey}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.typed.scaladsl.ActorSource
import akkastore.api.WatchEvent.{Completed, Failure, KeyRemoved, ValueUpdated}
import akkastore.api.{AkkaStore, KVPayload, Ok, WatchEvent}

import scala.async.Async._
import scala.concurrent.Future

class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  override def set(key: K, value: V): Future[Ok] = async {
    println("set key-val " + key + " " + value)
    val update: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ + (key, value))

    val result = await(replicator ? update)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in set")
        Ok
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }

  override def list: Future[List[KVPayload[K, V]]] = {
    listInner.map(m => m.map { case (key, value) => KVPayload(key, value) }.toList)
  }

  override def get(key: K): Future[Option[V]] = {
    println("Inside get ...." + key)
    listInner.map(_.get(key))
  }

  private def listInner: Future[Map[K, V]] = async {
    println("Inside innerList....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = await(replicator ? get)

    result match {
      case r @ Replicator.GetSuccess(k, v) =>
        val v = r.get(k)
        println("In innerList success...." + v.entries)
        v.entries
      //Handle exceptions GetFailure and Notfound
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  override def remove(key: K): Future[Ok] = async {
    val remove: ActorRef[UpdateResponse[LWWMap[K, V]]] => Update[LWWMap[K, V]] =
      Replicator.Update(DataKey, LWWMap.empty[K, V], Replicator.WriteLocal)(_ - key)

    val result = await(replicator ? remove)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("update success in remove")
        Ok
      case x => throw new RuntimeException(s"deletion failed due to: $x")
    }
  }

  override def watch(key: K): Source[WatchEvent[V], NotUsed] = {

    println("In watch Impl")

    /* A sample code that runs simple messages on stream.
      val akkaStoreSoure =
      Source
        .actorRef[WatchEvent[V]](Int.MaxValue, fail)
        .to(Sink.foreach(println))
        .run()

    Source(1 to 10)
      .map(x => akkaStoreSoure ! x)
      .runWith(Sink.ignore) */

    implicit val dd: Source[WatchEvent[V], NotUsed.type] = ActorSource
      .actorRef[Replicator.Changed[LWWMap[K, V]]](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        1024,
        OverflowStrategy.dropHead
      )
      .mapMaterializedValue { actorRef =>
        replicator ! Replicator.Subscribe[LWWMap[K, V]](DataKey, actorRef)
        println("In upcast .... ")
        NotUsed
      }
      .collect {
        case _ =>
          println("In collect ...")
          KeyRemoved
        // case c @ Replicator.Changed(dataKey) if c.get(dataKey).isDefined ⇒
        //ValueUpdated(c.get(dataKey).value.get)
        //case c @ Replicator.Changed(dataKey) ⇒ KeyRemoved
      }

    dd.to(Sink.foreach(println)).run()
    dd

  }

}
