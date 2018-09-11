package akkastore.client
import play.api.libs.json.{Format, Json}

case class Id(value: String)
case class Person(name: String, age: Int)

case class NumId(Value: Int)
case class NumStrDatails(name: String, phone: String)

trait TestJsonSupport {
  implicit val idFormat: Format[Id]         = Json.format[Id]
  implicit val personFormat: Format[Person] = Json.format[Person]

  implicit val numFormat: Format[NumId]                   = Json.format[NumId]
  implicit val numStrDatailsFormat: Format[NumStrDatails] = Json.format[NumStrDatails]
}
