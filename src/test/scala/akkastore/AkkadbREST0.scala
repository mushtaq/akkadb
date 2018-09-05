package akkadb

import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import akkadb.JsonSupport.AkkaStoreSet
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class AkkadbREST0 extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures with JsonSupport.JsonSupport {

  lazy val actorRuntime                  = new ActorRuntime(system.toTyped)
  lazy val akkDb: AkkaStore[String, String] = new AkkaStoreImpl("demo-store", actorRuntime)
  lazy val akkaDBRoutes                  = new AkkaStoreRoutes(akkDb)
  lazy val akkaDbServer                  = new AkkaStoreServer(akkaDBRoutes, actorRuntime)


  "AkkaDb Rest API" should {

    "POST set1.1" in {

      val akkadbSet = AkkaStoreSet("d", "500")

      Post("/akkastore/demo-store/set", akkadbSet) ~> akkaDBRoutes.route ~> check {
        //responseAs[String] shouldEqual "Successfully added to store - Key : d - Value : 500"
        responseAs[AkkaStoreSet] shouldEqual akkadbSet

        status shouldBe StatusCodes.Created
      }
    }
  }
}

//OLD test cases written by forming HTTP requests
//package akkadb
//
//import akka.actor.typed.scaladsl.adapter._
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import akka.util.ByteString
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.{Matchers, WordSpec}
//
//class AkkaStoreREST extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {
//
//  lazy val actorRuntime                  = new ActorRuntime(system.toTyped)
//  lazy val akkDb: AkkaStore[String, String] = new AkkaStoreImpl("demo-store", actorRuntime)
//  lazy val akkaDBRoutes                  = new AkkaStoreRoutes(akkDb)
//  lazy val akkaDbServer                  = new AkkaStoreServer(akkaDBRoutes, actorRuntime)
//
//  import HttpMethods._
//
//  "AkkaDb Rest API" should {
//    "POST set1" in {
//      val data = ByteString(s""" {"key":"a", "value":"100"} """)
//
//      val postRequest = HttpRequest(POST, "/akkastore/demo-store/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : a - Value : 100\""
//      }
//    }
//
//    "POST set2" in {
//      val data = ByteString(s""" {"key":"b", "value":"200"} """)
//
//      val postRequest = HttpRequest(POST, "/akkastore/demo-store/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : b - Value : 200\""
//      }
//    }
//
//    "POST set3" in {
//      val data = ByteString(s""" {"key":"c", "value":"300"} """)
//
//      val postRequest = HttpRequest(POST, "/akkastore/demo-store/set", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "\"Successfully added to store - Key : c - Value : 300\""
//      }
//    }
//
//    Thread.sleep(3000)
//
//    "GET list1" in {
//      val getRequest = HttpRequest(GET, uri = "/akkastore/demo-store/list")
//      getRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual
//          """{""" + "\n" +
//            """  "a" : "100",""" + "\n" +
//            """  "b" : "200",""" + "\n" +
//            """  "c" : "300"""" + "\n" +
//            """}"""
//      }
//
//    }
//
//    "POST get" in {
//      val data = ByteString(s""" {"key":"c"} """)
//
//      val postRequest = HttpRequest(POST, "/akkastore/demo-store/get", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "\"300\""
//      }
//    }
//
//
//    "POST remove" in {
//      val data = ByteString(s""" {"key":"b"} """)
//
//      val postRequest = HttpRequest(POST, "/akkastore/demo-store/remove", entity = HttpEntity(MediaTypes.`application/json`, data))
//      postRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "\"Successfully removed from store key - b\""
//      }
//    }
//    "GET list2" in {
//      val getRequest = HttpRequest(GET, uri = "/akkastore/demo-store/list")
//      getRequest ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual
//          """{""" + "\n" +
//            """  "a" : "100",""" + "\n" +
//            """  "c" : "300"""" + "\n" +
//            """}"""
//      }
//
//    }
//
//  }
//}
//
