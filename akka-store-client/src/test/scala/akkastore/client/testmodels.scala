package akkastore.client
import play.api.libs.json.{Format, Json}

case class Id(value: String)
case class Person(name: String, age: Int)

trait TestJsonSupport {
  implicit val idFormat: Format[Id]         = Json.format[Id]
  implicit val personFormat: Format[Person] = Json.format[Person]
}
