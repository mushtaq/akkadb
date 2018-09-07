package akkastore.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akkastore.api.{AkkaStore, JsonSupport, KVPayload, Ok}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.Format

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class AkkaStoreClient[K: Format, V: Format](baseUri: String)(implicit actorSystem: ActorSystem)
    extends AkkaStore[K, V]
    with JsonSupport
    with PlayJsonSupport {

  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext       = actorSystem.dispatcher

  override def list: Future[Map[K, V]] = async {
    val request  = HttpRequest().withUri(s"$baseUri/demo-store/list")
    val response = await(Http().singleRequest(request))
    await(Unmarshal(response.entity).to[List[KVPayload[K, V]]]).map(x => (x.key, x.value)).toMap
  }

  override def get(key: K): Future[Option[V]] = async {

    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/demo-store/get")
      .withEntity(await(Marshal(key).to[MessageEntity]))

    println(request)

    val response = await(Http().singleRequest(request))

    await(Unmarshal(response.entity).to[Option[V]])
  }

  override def set(key: K, value: V): Future[Ok] = async {
    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/demo-store/set")
      .withEntity(await(Marshal(KVPayload(key, value)).to[MessageEntity]))

    println(request)

    val response = await(Http().singleRequest(request))

    response.discardEntityBytes()

    response.status match {
      case StatusCodes.OK => Ok
      case x              => throw new RuntimeException(response.entity.toString)
    }
  }

  override def remove(key: K): Future[Ok] = async {

    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUri/demo-store/remove")
      .withEntity(await(Marshal(key).to[MessageEntity]))

    val response = await(Http().singleRequest(request))

    response.discardEntityBytes()

    response.status match {
      case StatusCodes.OK => Ok
      case x              => throw new RuntimeException(response.entity.toString)
    }
  }
}
