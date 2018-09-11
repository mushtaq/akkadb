package akkastore.client
import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}

class ClientTest extends FunSuite with Matchers with TestJsonSupport {

  test("dd") {
    implicit val actorSystem: ActorSystem = ActorSystem("test")
    val client                            = new AkkaStoreClient[Id, Person]("http://localhost:8080/akkastore")

    client.set(Id("123"), Person("leena", 99)).get

    println(client.get(Id("123")).get)

  }

  implicit class BlockingFuture[T](f: Future[T]) {
    def get: T = Await.result(f, 5.seconds)
  }
}
