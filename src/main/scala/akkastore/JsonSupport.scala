package akkastore
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Json, OFormat}

object JsonSupport {

  case class AkkaStoreSet(key: String, value: String)
  case class AkkaStoreGet(key: String)
  case class AkkaStoreRemove(key: String)

  trait JsonSupport extends PlayJsonSupport {
    implicit val setProtocol: OFormat[AkkaStoreSet]       = Json.format[AkkaStoreSet]
    implicit val getProtocol: OFormat[AkkaStoreGet]       = Json.format[AkkaStoreGet]
    implicit val removeProtocol: OFormat[AkkaStoreRemove] = Json.format[AkkaStoreRemove]
  }
}
