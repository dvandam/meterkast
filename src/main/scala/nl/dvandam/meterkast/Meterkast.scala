package nl.dvandam.meterkast

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object Meterkast extends App {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val valuePattern = """((?:hoog|laag)[pu]): (\d+)""".r

  val done: Future[Done] = Source.fromIterator { () => StdInReader }
    .scan(Builder(Map.empty, None)) {
      case (_, "start") => Builder(Map.empty, None)
      case (Builder(data, _), "end") => Builder(Map.empty, for {
        usageLow <- data.get("laagu")
        usageHigh <- data.get("hoogu")
        productionLow <- data.get("laagp")
        productionHigh <- data.get("hoogp")
      } yield {
        Record(Usage(usageLow, usageHigh), Production(productionLow, productionHigh))
      })
      case (Builder(data, _), valuePattern(field, value)) => Builder(data + (field -> value.toInt), None)
      case (builder, _) => builder.copy(record = None)
    }
    .collect {
      case Builder(_, Some(record)) => record
    }
    .runForeach(i => println(i))(materializer)

  done.onComplete(_ => system.terminate())
}
