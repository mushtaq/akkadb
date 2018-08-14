package example.typed

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import example.typed.AkkaDB.{ActionOnDB, GetAll, Set}
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMapKey, Replicator}
import akka.cluster.ddata.Replicator.Get
import akka.cluster.ddata.typed.scaladsl.Replicator

import scala.concurrent.duration._

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

      //val systemA: ActorSystem[TestAkkaDB.AkkaDbMsg] = akka.actor.typed.ActorSystem(TestAkkaDB.behavior, "hello", config)
      //systemA ! TestAkkaDB.Test

      val system1: ActorSystem[AkkaDB.ActionOnDB] = akka.actor.typed.ActorSystem(AkkaDB.bhvrAkkaDD, "helloDB", config)

      // implicit val cluster: Cluster = akka.cluster.Cluster(system1.toUntyped)
      //val replicator = DistributedData(system1).replicator

      implicit val timeout   = Timeout(13.seconds)
      implicit val scheduler = system1.scheduler

      system1 ! Set("set", 10)
      system1 ! Set("set", 20)

      val eventualDB = system1 ? { ref: ActorRef[ActionOnDB] =>
        GetAll(ref)
      }

      implicit val ec = system1.executionContext
      eventualDB.foreach { x =>
        println("**********************" + x)
      }

    }
  }

}
