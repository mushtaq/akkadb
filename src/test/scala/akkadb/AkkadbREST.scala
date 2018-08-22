package akkadb

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.server.Directives._

class AkkadbREST extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  private val wiringTest = new Wiring
  wiringTest.akkaDbServer.start()

  //override val system = actor.ActorSystem("akka-store")

  Thread.sleep(3000)

  import HttpMethods._

  "AkkaDb Rest API" should {
    "POST set1" in {
      val data = ByteString(s""" {"key":"a", "value":"100"} """)

      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
      postRequest ~> wiringTest.akkaDBRoutes.route ~> check {
        responseAs[String] shouldEqual "\"Successfully added to store - Key : a - Value : 100\""
      }
    }

    "POST set2" in {
      val data = ByteString(s""" {"key":"b", "value":"200"} """)

      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"Successfully added to store - Key : b - Value : 200\""
      }
    }

    "POST set3" in {
      val data = ByteString(s""" {"key":"c", "value":"300"} """)

      val postRequest = HttpRequest(POST, "/akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, data))
      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"Successfully added to store - Key : c - Value : 300\""
      }
    }

    /* "GET list1" in {
      val getRequest = HttpRequest(GET, uri = "/akkadb/demo-db/list")
      getRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"In list\""
      }

    }

    "POST get" in {
      val data = ByteString(s""" {"key":"c"} """)

      val postRequest = HttpRequest(POST, "/akkadb/demo-db/get", entity = HttpEntity(MediaTypes.`application/json`, data))
      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"In get\""
      }
    }

    "POST remove" in {
      val data = ByteString(s""" {"key":"b"} """)

      val postRequest = HttpRequest(POST, "/akkadb/demo-db/remove", entity = HttpEntity(MediaTypes.`application/json`, data))
      postRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"Successfully removed from store key - b\""
      }
    }

    "GET list2" in {
      val getRequest = HttpRequest(GET, uri = "/akkadb/demo-db/list")
      getRequest ~> TestAkkaDbRoutes.routeTest ~> check {
        responseAs[String] shouldEqual "\"In list\""
      }

    }*/

  }
}
