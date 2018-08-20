package akkadb
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Json, OFormat}

object JsonSupport {

  case class AkkadbSet(key: String, value: String)
  case class AkkadbGet(key: String)
  case class AkkadbRemove(key: String)

  trait JsonSupport extends PlayJsonSupport {
    implicit val setProtocol: OFormat[AkkadbSet]       = Json.format[AkkadbSet]
    implicit val getProtocol: OFormat[AkkadbGet]       = Json.format[AkkadbGet]
    implicit val removeProtocol: OFormat[AkkadbRemove] = Json.format[AkkadbRemove]
  }
}
