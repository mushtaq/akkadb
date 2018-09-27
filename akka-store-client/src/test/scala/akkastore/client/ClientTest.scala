package akkastore.client
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ClientTest extends FunSuite with Matchers with TestJsonSupport {

  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val mat: Materializer        = ActorMaterializer()

  test("Test Model1 - key of type Int") {

    val client = new AkkaStoreClient[NumId, NumStrDatails]("http://localhost:8080/akkastore/demo-store1")

    client.set(NumId(1), NumStrDatails("name1", "9898989")).get

    //Testing watchKey event
    //Await.result(client.watch(NumId(1)).runForeach(println), 5.seconds)
    val killswitch = client
      .watch(NumId(1))
      .toMat(Sink.foreach(println))(Keep.left)
      .run()

    actorSystem.scheduler.scheduleOnce(20.seconds) {
      killswitch.shutdown()
    }

    Thread.sleep(2000)

    client.set(NumId(1), NumStrDatails("222name1", "22229898989")).get
    client.set(NumId(2), NumStrDatails("name2", "878787")).get
    client.set(NumId(3), NumStrDatails("name3", "989890")).get
    client.set(NumId(4), NumStrDatails("name4", "454545")).get
    client.set(NumId(5), NumStrDatails("name5", "121121")).get
    println(client.get(NumId(1)).get)
    client.remove(NumId(2))
    println((client.list).get)
  }
  //Thread.sleep(2000)

  test("Test Model2 - key of type String") {

    val client2 = new AkkaStoreClient[Id, Person]("http://localhost:8080/akkastore/demo-store2")

    client2.set(Id("001"), Person("name1", 10)).get
    client2.set(Id("002"), Person("name2", 20)).get
    client2.set(Id("003"), Person("name3", 30)).get
    client2.set(Id("004"), Person("name4", 40)).get
    client2.set(Id("005"), Person("name5", 50)).get
    println(client2.get(Id("001")).get)
    client2.remove(Id("002"))
    println((client2.list).get)

  }

  implicit class BlockingFuture[T](f: Future[T]) {
    def get: T = Await.result(f, 5.seconds)
  }
}
