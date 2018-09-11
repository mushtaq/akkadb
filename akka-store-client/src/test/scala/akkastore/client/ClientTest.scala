package akkastore.client
import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}

class ClientTest extends FunSuite with Matchers with TestJsonSupport {

  test("dd") {
    implicit val actorSystem: ActorSystem = ActorSystem("test")

    //Testing with model that has key of type Int
    val client = new AkkaStoreClient[NumId, NumStrDatails]("http://localhost:8080/akkastore/demo-store")

    client.set(NumId(1), NumStrDatails("name1", "9898989")).get
    client.set(NumId(2), NumStrDatails("name2", "878787")).get
    client.set(NumId(3), NumStrDatails("name3", "989890")).get
    client.set(NumId(4), NumStrDatails("name4", "454545")).get
    client.set(NumId(5), NumStrDatails("name5", "121121")).get

    println(client.get(NumId(1)).get)
    client.remove(NumId(2))
    println((client.list).get)

    Thread.sleep(2000)

    //Testing with model that has key of type String

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
