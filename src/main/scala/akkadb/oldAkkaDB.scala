//package akkadb
//
//import akka.actor.typed.scaladsl.Behaviors
//import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
//import akka.actor.typed.scaladsl.adapter._
//import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
//import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, UpdateResponse}
//import akka.cluster.ddata._
//import akka.cluster.Cluster
//import akka.cluster.ddata.Replicator.ReadLocal
//import akka.cluster.ddata.protobuf.msg.ReplicatedDataMessages
//
//object oldAkkaDB {
//
//  sealed trait ActionOnDB
//  final case class Set(key: String, value: Int)                    extends ActionOnDB
//  final case class GetAll(replyTo: ActorRef[ActionOnDB])           extends ActionOnDB
//  final case class Get(key: String, replyTo: ActorRef[ActionOnDB]) extends ActionOnDB
//  final case class Remove(key: String)                             extends ActionOnDB
//  //response messages
//  final case class ResponseGet(mapItems: Map[String, Int]) extends ActionOnDB
//
//  private sealed trait InternalMsg                                                                  extends ActionOnDB
//  private case class InternalUpdateResponse[A <: ReplicatedData](rsp: Replicator.UpdateResponse[A]) extends InternalMsg
//  private case class InternalGetResponse[A <: ReplicatedData](rsp: Replicator.GetResponse[A])       extends InternalMsg
//
//  val bhvrAkkaDD: Behavior[oldAkkaDB.ActionOnDB] = Behaviors.receive { (ctx, msg) ⇒
//    implicit val cluster: Cluster = akka.cluster.Cluster(ctx.system.toUntyped)
//
//    val replicator: ActorRef[Replicator.Command] = DistributedData(ctx.system).replicator
//
//    //This key should hv some additional variable added to it like say table name that comes from outside this object
//    val DataKey = LWWMapKey[String, Int]("LWWMapKey")
//
//    val getResponseAdapter: ActorRef[Replicator.GetResponse[LWWMap[String, Int]]] =
//      ctx.messageAdapter(InternalGetResponse.apply)
//
//    val updateResponseAdapter: ActorRef[Replicator.UpdateResponse[LWWMap[String, Int]]] =
//      ctx.messageAdapter(InternalUpdateResponse.apply)
//
//    msg match {
//
//      case Set(key, val1) =>
//        println("Adding key and akkDb *** ")
//        val update =
//          Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal, updateResponseAdapter)(_ + (key, val1))
//        replicator ! update
//        println("AfterUpdate *** ")
//        Behaviors.same
//
//      case Remove(key) =>
//        //println("before removing ***")
//        val remove =
//          Replicator.Update(DataKey, LWWMap.empty[String, Int], Replicator.WriteLocal, updateResponseAdapter)(_ - (key))
//        replicator ! remove
//        Behaviors.same
//
//      case Get(key, replyTo) =>
//        //replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo))
//        replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo, Some(key)))
//        Behaviors.same
//
//      case GetAll(replyTo) =>
//        replicator ! Replicator.Get(DataKey, ReadLocal, getResponseAdapter, Some(replyTo, None))
//        Behaviors.same
//
//      case internal: InternalMsg ⇒
//        internal match {
//          case InternalUpdateResponse(_) ⇒
//            //println("&&&&&&&&&& from intenal update response")
//            Behaviors.same // ok
//
//          case InternalGetResponse(
//              rsp @ Replicator.GetSuccess(DataKey,
//                                          Some((replyTo: ActorRef[ActionOnDB] @unchecked, key: Option[String] @unchecked)))
//              ) ⇒
//            val values = rsp.get(DataKey)
//            // You will get this message in case of GetAll and Get both.
//            // In case of Get we also want to pass key from Get to here to extract precise akkDb
//            val entries: Map[String, Int] = key match {
//              case Some(k) => Map(k -> values.entries(k))
//              case _       => values.entries
//
//            }
//            //println("Current Elements ***: {}" + values.entries.toSet.toString())
//            replyTo ! ResponseGet(entries)
//            Behaviors.same
//
//          case InternalGetResponse(rsp) ⇒
//            Behaviors.unhandled // not dealing with failures
//            Behaviors.same
//        }
//
//      case _ =>
//        println(msg.toString + "Duh...") //Some other msg than DB operations
//        Behaviors.same
//
//    }
//  }
//
//}
//Test pattern temp with TestAkkaRoutes
////
//package akkadb
//
//import akka.http.scaladsl.marshalling.Marshal
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import akka.util.ByteString
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.{Matchers, WordSpec}
//import akka.http.scaladsl.server.Directives._
//
//final case class setRequest(k: String, v: String)
//
//class AkkadbREST extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {
//
//  //private val wiringTest = new Wiring
//  //wiringTest.akkaDbServer.start()
//
//  //override val system = actor.ActorSystem("akka-store")
//
//  Thread.sleep(3000)
//
//  import HttpMethods._
//
//  "AkkaDb Rest API" should {
//    "POST set1" in {
//      val data = ByteString(s""" {"key":"a", "value":"100"} """)
//
//      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : a - Value : 100\""
//      }
//    }
//
//    "POST set2" in {
//      val data = ByteString(s""" {"key":"b", "value":"200"} """)
//
//      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : b - Value : 200\""
//      }
//    }
//
//    "POST set3" in {
//      val data = ByteString(s""" {"key":"c", "value":"300"} """)
//
//      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : c - Value : 300\""
//      }
//    }
//
//    "GET list1" in {
//      val getRequest = HttpRequest(GET, uri = "/akkadb/demo-db/list")
//      getRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"In list\""
//      }
//
//    }
//
//    "POST get" in {
//      val data = ByteString(s""" {"key":"c"} """)
//
//      val postRequest = HttpRequest(POST, "/akkadb/demo-db/get", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"In get\""
//      }
//    }
//
//    "POST remove" in {
//      val data = ByteString(s""" {"key":"b"} """)
//
//      val postRequest = HttpRequest(POST, "/akkadb/demo-db/remove", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"Successfully removed from store key - b\""
//      }
//    }
//
//    "GET list2" in {
//      val getRequest = HttpRequest(GET, uri = "/akkadb/demo-db/list")
//      getRequest ~> TestAkkaDbRoutes.routeTest ~> check {
//        responseAs[String] shouldEqual "\"In list\""
//      }
//
//    }
//
//  }
//}
