package example.typed

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object AkkaDBapp {

  def main(args: Array[String]): Unit = {
    println("from AkkaDB main")

    //startup(Seq("2551", "2552", "0")) - use later for multi node testing
    if (args.isEmpty)
      startup(Seq("2551"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {

    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory
        .parseString(
          s"""
           |akka.remote.netty.tcp.port=$port
           |akka.remote.artery.canonical.port=$port
           |""".stripMargin
        )
        .withFallback(ConfigFactory.load())

      val systemA: ActorSystem[TestAkkaDB.AkkaDbMsg] = akka.actor.typed.ActorSystem(TestAkkaDB.behavior, "hello", config)

      systemA ! TestAkkaDB.Test
    }
  }
}
