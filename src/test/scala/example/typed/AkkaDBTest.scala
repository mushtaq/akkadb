package example.typed
import akka.Done
import akka.actor.typed.ActorSystem
import akka.japi.Option.Some
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AkkaDBTest extends FunSuite with BeforeAndAfter with Matchers {

  //Actor system creation before actual tests
  var obj: AkkaDistDB = _

  val port: String = "2551"
  val config = ConfigFactory
    .parseString(
      s"""
           |akka.remote.netty.tcp.port=$port
           |akka.remote.artery.canonical.port=$port
           |""".stripMargin
    )
    .withFallback(ConfigFactory.load())
  val system1: ActorSystem[AkkaDB.ActionOnDB] = akka.actor.typed.ActorSystem(AkkaDB.bhvrAkkaDD, "helloDB", config)
  obj = new AkkaDistImpl(system1)

  //Actual AkkaDB API testing
  test("Test set API") {
    Await.result(obj.set("set", 10), 500 millis) shouldBe (Done)
  }

  test("Test list API") {
    Await.result(obj.list, 500 millis) shouldBe (Map("set" -> 10))
  }

  test("Tst get API") {
    Await.result(obj.get("set"), 500 millis) shouldBe (Option(10))
  }

  after {
    system1.terminate()
  }

}
