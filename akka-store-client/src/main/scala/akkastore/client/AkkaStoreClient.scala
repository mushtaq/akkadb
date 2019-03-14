package akkastore.client

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{ActorMaterializer, KillSwitch, KillSwitches, Materializer}
import akkastore.api._
import akkastore.common.PlayJsonSupport
import play.api.libs.json.{Format, Json}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class AkkaStoreClient[K: Format, V: Format](baseUri: String)(implicit actorSystem: ActorSystem)
    extends AkkaStore[K, V]
    with JsonSupport
    with PlayJsonSupport {

  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext       = actorSystem.dispatcher

  override def list: Future[List[KVPayload[K, V]]] = async {
    val request  = HttpRequest().withUri(s"$baseUri/list")
    val response = await(Http().singleRequest(request))
    await(Unmarshal(response.entity).to[List[KVPayload[K, V]]])
  }

  override def get(key: K): Future[Option[V]] = async {

    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/get")
      .withEntity(await(Marshal(key).to[MessageEntity]))

    println(request)

    val response = await(Http().singleRequest(request))

    await(Unmarshal(response.entity).to[Option[V]])
  }

  override def set(key: K, value: V): Future[Done] = async {
    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/set")
      .withEntity(await(Marshal(KVPayload(key, value)).to[MessageEntity]))

    println(request)

    val response = await(Http().singleRequest(request))

    response.discardEntityBytes()

    response.status match {
      case StatusCodes.OK => Done
      case _              => throw new RuntimeException(response.entity.toString)
    }
  }

  override def remove(key: K): Future[Done] = async {

    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/remove")
      .withEntity(await(Marshal(key).to[MessageEntity]))

    val response = await(Http().singleRequest(request))

    response.discardEntityBytes()

    response.status match {
      case StatusCodes.OK => Done
      case x              => throw new RuntimeException(response.entity.toString)
    }
  }

  override def watch(key: K): Source[WatchEvent[V], KillSwitch] = {
    import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._

    val sssFutureStream = async {
      val request = HttpRequest()
        .withMethod(HttpMethods.POST)
        .withUri(s"$baseUri/watch")
        .withEntity(await(Marshal(key).to[MessageEntity]))

      println(request)

      val response = await(Http().singleRequest(request))
      println(response)
      await(Unmarshal(response.entity).to[Source[ServerSentEvent, NotUsed]])
    }

    Source
      .fromFutureSource(sssFutureStream)
      .map { serverSentEvent =>
        println("Event data in client..." + serverSentEvent.data)
        Json.parse(serverSentEvent.data).as[WatchEvent[V]]
      }
      .viaMat(KillSwitches.single)(Keep.right)
  }
}
