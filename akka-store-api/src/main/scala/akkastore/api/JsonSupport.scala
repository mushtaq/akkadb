package akkastore.api

import julienrf.json.derived
import play.api.libs.json._

trait JsonSupport {
  implicit def payLoadFormat[K: Format, V: Format]: OFormat[KVPayload[K, V]] = Json.format[KVPayload[K, V]]
  implicit def watchFormat[T: Format]: Format[WatchEvent[T]]                 = derived.flat.oformat((__ \ "type").format[String])
}
