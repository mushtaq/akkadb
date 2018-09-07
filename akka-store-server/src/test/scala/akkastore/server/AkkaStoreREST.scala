//package akkastore.server
//
//import akka.actor.typed.scaladsl.adapter._
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import akkastore.api.{AkkaStore, JsonSupport}
//import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.{Matchers, WordSpec}
//import play.api.libs.json.JsValue
//
//class AkkaStoreREST
//    extends WordSpec
//    with Matchers
//    with ScalatestRouteTest
//    with ScalaFutures
//    with JsonSupport
//    with PlayJsonSupport {
//
//  lazy val actorRuntime                       = new ActorRuntime(system.toTyped)
//  lazy val akkDb: AkkaStore[JsValue, JsValue] = new AkkaStoreImpl("demo-store", actorRuntime)
//  lazy val akkaDBRoutes                       = new AkkaStoreRoutes(akkDb)
//  lazy val akkaDbServer                       = new AkkaStoreServer(akkaDBRoutes, actorRuntime)
//
//  import HttpMethods._
//
//  "AkkaDb Rest API" should {
//    "POST set1" in {
//      val akkastoreset = AkkaStoreSet("a", "100")
//
//      Post("/akkastore/demo-store/set", akkastoreset) ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "Successfully added to store - Key : a - Value : 100"
//      }
//    }
//
//    "POST set2" in {
//      val akkastoreset = AkkaStoreSet("b", "200")
//
//      Post("/akkastore/demo-store/set", akkastoreset) ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "Successfully added to store - Key : b - Value : 200"
//      }
//    }
//
//    "POST set3" in {
//      val akkastoreset = AkkaStoreSet("c", "300")
//
//      Post("/akkastore/demo-store/set", akkastoreset) ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "Successfully added to store - Key : c - Value : 300"
//      }
//    }
//
//    Thread.sleep(2000)
//
//    "GET list1" in {
//      Get("/akkastore/demo-store/list") ~> akkaDBRoutes.route ~> check {
//        val extractedResponse: String = responseEntity.toString.substring(responseEntity.toString.indexOf('{'))
//
//        extractedResponse shouldEqual
//        """{""" + "\n" +
//        """  "a" : "100",""" + "\n" +
//        """  "b" : "200",""" + "\n" +
//        """  "c" : "300"""" + "\n" +
//        """})"""
//      }
//    }
//
//    "POST get" in {
//      val akkastoreget = AkkaStoreGet("c")
//
//      Post("/akkastore/demo-store/get", akkastoreget) ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "300"
//      }
//    }
//
//    "POST remove" in {
//      val akkastoreremove = AkkaStoreRemove("b")
//
//      Post("/akkastore/demo-store/remove", akkastoreremove) ~> akkaDBRoutes.route ~> check {
//        responseAs[String] shouldEqual "Successfully removed from store key - b"
//      }
//    }
//
//    "GET list2" in {
//      val getRequest = HttpRequest(GET, uri = "/akkastore/demo-store/list")
//      getRequest ~> akkaDBRoutes.route ~> check {
//        val extractedResponse: String = responseEntity.toString.substring(responseEntity.toString.indexOf('{'))
//
//        extractedResponse shouldEqual
//        """{""" + "\n" +
//        """  "a" : "100",""" + "\n" +
//        """  "c" : "300"""" + "\n" +
//        """})"""
//      }
//    }
//  }
//}
