package akkastore.client

import play.api.libs.json.{Format, Json}

case class Id(value: String)
object Id {
  implicit val idFormat: Format[Id] = Json.format[Id]
}

case class Person(name: String, age: Int)
object Person {
  implicit val personFormat: Format[Person] = Json.format[Person]
}

case class NumId(Value: Int)
object NumId {
  implicit val numFormat: Format[NumId] = Json.format[NumId]
}

case class NumStrDatails(name: String, phone: String)
object NumStrDatails {
  implicit val numStrDatailsFormat: Format[NumStrDatails] = Json.format[NumStrDatails]
}

trait TestJsonSupport {}
