package example.typed

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaDBTest extends FunSuite with BeforeAndAfterAll with Matchers {

  val system          = ActorSystem(Behaviors.empty, "test")
  private val runtime = new ActorRuntime(system)

  val obj: AkkaStore[String, Int] = new AkkaStoreImpl("demo-db", runtime)

  test("Test set API") {
    Await.result(obj.set("a", 10), 2.seconds) shouldBe Done
    Await.result(obj.set("b", 20), 2.seconds) shouldBe Done
  }

  test("Test list API") {
    Await.result(obj.list, 2.seconds) shouldBe Map("a" -> 10, "b" -> 20)
  }

  test("Test get API") {
    Await.result(obj.get("a"), 2.seconds) shouldBe Option(10)
    Await.result(obj.get("b"), 2.seconds) shouldBe Option(20)
  }

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 2.seconds)
  }
}
