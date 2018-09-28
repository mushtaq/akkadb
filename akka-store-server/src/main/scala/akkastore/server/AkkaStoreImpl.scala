package akkastore.server

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.ddata._
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.typed.scaladsl.ActorSource
import akkastore.api.WatchEvent.{KeyRemoved, ValueUpdated}
import akkastore.api._

import scala.async.Async._
import scala.concurrent.Future

/**
 * Holds key-value pair in CRDT map of type LWWMap
 *
 * @param dbName - Akka store name.
 * @param runtime - Holds all system specific data needed. like replicator, ec, materializer etc.
 * @tparam K - Key of type K.
 * @tparam V - Value of type V.
 */
class AkkaStoreImpl[K, V](dbName: String, runtime: ActorRuntime) extends AkkaStore[K, V] {
  import runtime._

  val DataKey: LWWMapKey[K, V] = LWWMapKey[K, V](dbName)

  /**
   * set : will either add a new key-value pair or update existing key with specified value in akka store
   */
  override def set(key: K, value: V): Future[Ok] = async {

    //for key watching
    await(updateWatchKey(key, Some(value)))

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

  /**
   * list : Lists all the available key-value entries in CRDT map
   */
  override def list: Future[List[KVPayload[K, V]]] = {
    listInner.map(m => m.map { case (key, value) => KVPayload(key, value) }.toList)
  }

  /**
   * get : Will return value against key
   */
  override def get(key: K): Future[Option[V]] = {
    println("Inside get ...." + key)
    listInner.map(_.get(key))
  }

  /**
   * listInner : is a common function used by 'get' and 'list' functions.
   * replcator always returns entire list of key-value pairs. In case of 'get' we extract the value of specific key.
   * In list the result is forwarded by adding to payload.
   *
   */
  private def listInner: Future[Map[K, V]] = async {
    println("Inside innerList....")
    val get    = Replicator.Get(DataKey, Replicator.ReadLocal)
    val result = await(replicator ? get)

    result match {
      case r @ Replicator.GetSuccess(k, v) =>
        val v = r.get(k)
        println("In innerList success...." + v.entries)
        v.entries
      case x => throw new RuntimeException(s"Get failed due to: $x")
    }
  }

  /**
   * remove : will send update message to replicator to remove the specified key from akka store
   */
  override def remove(key: K): Future[Ok] = async {
    //for key watching -- with value as None.
    await(updateWatchKey(key, Option.empty[V]))

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

  /**
   * watch : will subscribe to replicator to watch a perticular key.
   * It will generate either ValueChanged or KeyRemoved message.
   * Which will be later pushed from server as server side message.
   */
  override def watch(key: K): Source[WatchEvent[V], KillSwitch] = {
    println("In watch Impl")

    val subscribekey: LWWRegisterKey[Option[V]] = LWWRegisterKey(key.toString)

    implicit val watchEvent: Source[WatchEvent[V], KillSwitch] = ActorSource
      .actorRef[Replicator.Changed[LWWRegister[Option[V]]]](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        1024,
        OverflowStrategy.dropHead
      )
      .mapMaterializedValue { actorRef =>
        replicator ! Replicator.Subscribe[LWWRegister[Option[V]]](subscribekey, actorRef)
        println("In materialize .... " + subscribekey)
      }
      .viaMat(KillSwitches.single)(Keep.right)
      /*.map { a =>
        println("*" * 80)
        println(a)
        println("*" * 80)
        a
      }*/
      .collect {
        case c @ Replicator.Changed(datakey) if (c.get(datakey).value.get == None) =>
          println("Key Deleted ... " + datakey)
          KeyRemoved //Some issues with replicator subscibing an event- when value against key is None. Need to look into it.
        case c @ Replicator.Changed(datakey) =>
          println("Value Changed ... " + datakey)
          ValueUpdated(c.get(datakey).value.get)
        case _ => throw new RuntimeException(s"Exeption in Subscribed collect..")
      }
    //watchEvent.to(Sink.foreach(println)).run()
    watchEvent
  }

  /**
   * updateWatchKey : This function is implemented for key watching.
   * This function with value as None will be called from remove of akka store function
   *
   * Every time we set or remove key in the main akka store, we also update a seperate key in CRDT as LWWRegister.
   * This key is used as subscribe key for replicator. This is to enable value changed/removed subscription events from replicator.
   */
  private def updateWatchKey(key: K, value: Option[V] = None): Future[Ok] = async {

    val watchKey: LWWRegisterKey[Option[V]] = LWWRegisterKey(key.toString)

    println("In UpdateWatchKey..." + key.toString + " " + value)

    val update: ActorRef[UpdateResponse[LWWRegister[Option[V]]]] => Update[LWWRegister[Option[V]]] =
      Replicator.Update(watchKey, LWWRegister(Option.empty[V]), Replicator.WriteLocal)(_ => LWWRegister(value))

    val result = await(replicator ? update)
    result match {
      case x: Replicator.UpdateSuccess[_] =>
        println("WatchKey update success..")
        Ok
      case x => throw new RuntimeException(s"update failed due to: $x")
    }
  }
}
