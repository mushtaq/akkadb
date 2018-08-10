package example.typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import example.typed.AkkaDB.{Get, GetAll, Remove, Set}

object TestAkkaDB {

  sealed trait AkkaDbMsg
  case object Test                                   extends AkkaDbMsg
  case class ResponseGet(mapItems: Map[String, Int]) extends AkkaDbMsg

  def behavior: Behavior[AkkaDbMsg] =
    Behaviors.setup[AkkaDbMsg] { ctx ⇒
      val getset = ctx.spawn(AkkaDB.bhvrAkkaDD, "getset")

      Behaviors.receive[AkkaDbMsg] { (ctx, msg) ⇒
        val replyAdapter = ctx.self

        msg match {

          case Test ⇒
            println(s"Testing ${ctx.self} Set and Get")
            getset ! Set("set", 10)
            getset ! Set("set", 20)
            getset ! Set("set1", 15)
            getset ! Set("set2", 12)
            getset ! Get("set", replyAdapter)
            getset ! Remove("set")
            getset ! GetAll(replyAdapter)

            Behaviors.same

          case ResponseGet(values) =>
            println("Get respoonse ***" + s"values= $values")
            Behaviors.same

        }

      }
    }
}
