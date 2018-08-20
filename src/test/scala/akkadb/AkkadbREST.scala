package akkadb

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class AkkadbREST extends WordSpec with Matchers with ScalatestRouteTest {

  private val wiring = new Wiring
  import wiring._

  "AkkaDB Rest API" should {
    "Post set" in {

      val jsonRequest = s"""
           |{
           |    "key":"test", "akkDb" : "val1"
           |}
        """.stripMargin

      val postRequest =
        HttpRequest(HttpMethods.POST, uri = "akkadb/demo-db/set", entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> akkaDBRoutes.route ~> check {
        //status.isSuccess() shouldEqual true
        responseAs[String] shouldEqual "Successfully added to store - Key : test - Value : val1"
      }
    }

  }

}
