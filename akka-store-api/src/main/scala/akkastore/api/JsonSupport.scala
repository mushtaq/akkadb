package akkastore.api
import play.api.libs.json.{Format, Json, OFormat}

trait JsonSupport {
  implicit def payLoadFormat[K: Format, V: Format]: OFormat[KVPayload[K, V]] = Json.format[KVPayload[K, V]]
  implicit def keyPayLoadFormat[K: Format]: OFormat[KPayload[K]]             = Json.format[KPayload[K]]

}
