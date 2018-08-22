package akkadb

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaDBTest extends FunSuite with BeforeAndAfterAll with Matchers {

  val system          = ActorSystem(Behaviors.empty, "test")
  private val runtime = new ActorRuntime(system)

  val objInt: AkkaDB[String, Int] = new AkkaDBImpl("demo-db-IntVal", runtime)

  test("Test set API for Int") {
    Await.result(objInt.set("a", 10), 2.seconds) shouldBe Done
    Await.result(objInt.set("b", 20), 2.seconds) shouldBe Done
  }

  test("Test list API for Int") {
    Await.result(objInt.list, 2.seconds) shouldBe Map("a" -> 10, "b" -> 20)
  }

  test("Test get API for Int") {
    Await.result(objInt.get("a"), 2.seconds) shouldBe Option(10)
    Await.result(objInt.get("b"), 2.seconds) shouldBe Option(20)
  }

  val objStr: AkkaDB[String, String] = new AkkaDBImpl("demo-db-StrVal", runtime)

  test("Test set API for Str") {
    Await.result(objStr.set("a", "leena1"), 2.seconds) shouldBe Done
    Await.result(objStr.set("b", "leena2"), 2.seconds) shouldBe Done
  }

  test("Test list API Str") {
    Await.result(objStr.list, 2.seconds) shouldBe Map("a" -> "leena1", "b" -> "leena2")
  }

  test("Test get API Str") {
    Await.result(objStr.get("a"), 2.seconds) shouldBe Option("leena1")
    Await.result(objStr.get("b"), 2.seconds) shouldBe Option("leena2")
  }

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 2.seconds)
  }
}
