package akkastore.server

import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akkastore.api.{JsonSupport, KPayload, KVPayload}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json
import play.api.libs.json.{Format, JsValue, Json, OFormat}

case class KVJson(key: String, value: String)

class AkkaStoreREST
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with ScalaFutures
    with JsonSupport
    with PlayJsonSupport {

  lazy val actorRuntime = new ActorRuntime(system.toTyped)
  lazy val akkaDBRoutes = new AkkaStoreRoutes(actorRuntime)
  lazy val akkaDbServer = new AkkaStoreServer(akkaDBRoutes, actorRuntime)

  "AkkaDb Rest API" should {
    "POST set1" in {

      /*//try outs of forming Json payload in different ways
      //case class KVJson(key: String, value: String)
      implicit val kvFormat: Format[KVJson] = Json.format[KVJson]
      //json as string
      val jsonSetValues: JsValue = Json.parse("""
        {
        "key" : "a",
        "value" : "100"
        }""")

      //passing json object
      val jsonObj = Json.obj("key" -> "a", "value" -> "100")

      //converting scala data structure to json
      val kvJson = KVJson(key = "a", value = "100")
      val jsonTo = Json.toJson[KVJson](kvJson)
       */
      val payload = KVPayload("a", "100")
      Post("/akkastore/demo-store/set", payload) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"a""""
      }
    }

    "POST set2" in {

      Post("/akkastore/demo-store/set", KVPayload("b", "200")) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"b""""
      }
    }

    "POST set3" in {

      Post("/akkastore/demo-store/set", KVPayload("c", "300")) ~> akkaDBRoutes.route ~> check {

        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"c""""
      }
    }

    Thread.sleep(1000)

    "GET list1" in {

      Get("/akkastore/demo-store/list") ~> akkaDBRoutes.route ~> check {

        status shouldBe StatusCodes.OK

        val expectedList = List(KVPayload("a", "100"), KVPayload("b", "200"), KVPayload("c", "300"))
        val r            = entityAs[List[KVPayload[String, String]]]
        r.toSet shouldEqual expectedList.toSet
      }
    }

    "POST get" in {

      Post("/akkastore/demo-store/get", "c") ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "300"
      }
    }

    "POST remove" in {

      Post("/akkastore/demo-store/remove", KPayload("b")) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Successfully removed key=" + """"b""""
      }
    }

    "GET list2" in {

      Get("/akkastore/demo-store/list") ~> akkaDBRoutes.route ~> check {

        status shouldBe StatusCodes.OK

        val expectedList = List(KVPayload("a", "100"), KVPayload("c", "300"))
        val r            = entityAs[List[KVPayload[String, String]]]
        r.toSet shouldEqual expectedList.toSet
      }
    }

  }

  "Watch Key API" should {
    "POST set a1" in {

      val payload = KVPayload("a", "100")
      Post("/akkastore/demo-store/set", payload) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"a""""
      }
    }

    Thread.sleep(1000)
    /*
    "GET set a1" in {

      Post("/akkastore/demo-store/get", "a") ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "100"
      }
    }
     */

    "POST watch  1" in {
      Post("/akkastore/demo-store/watch", KPayload("a")) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        // responseAs[String] shouldEqual "Successfully watching key=" + """"a""""
      }
    }

    /*
    Thread.sleep(1000)
    "POST set a2" in {

      val payload = KVPayload("a", "200")
      Post("/akkastore/demo-store/set", payload) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"a""""
      }
    }
     */
    Thread.sleep(1000)

    /*   "POST remove2" in {

      Post("/akkastore/demo-store/remove", KPayload("a")) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Successfully removed key=" + """"a""""
      }
    }*/

    Thread.sleep(3000)
  }

  "Watch Key API 2" should {

    "POST watch 2" in {

      Post("/akkastore/demo-store/watch", KPayload("a")) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Successfully watching key=" + """"a""""
      }
    }

    Thread.sleep(1000)

    "POST set a10" in {

      val payload = KVPayload("a", "200")
      Post("/akkastore/demo-store/set", payload) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"a""""
      }
    }
    Thread.sleep(1000)

    /* "POST set a30" in {

      val payload = KVPayload("a", "300")
      Post("/akkastore/demo-store/set", payload) ~> akkaDBRoutes.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual s"Successfully set value for key=" + """"a""""
      }
    }
    Thread.sleep(3000)
   */
  }
}
