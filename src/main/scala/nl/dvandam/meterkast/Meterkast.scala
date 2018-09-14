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

  val valuePattern = """1-0:([12].8.[12]).(\d+).(\d+).kWh.*""".r

  val done: Future[Done] = Source.fromIterator { () => StdInReader }
    .scan(Builder(Map.empty, None)) {
      case (_, "/ISk5\\2MT382-1003") => Builder(Map.empty, None) // start
      case (Builder(data, _), "!") => Builder(Map.empty, for { // end
        usageLow <- data.get("1.8.1")
        usageHigh <- data.get("1.8.2")
        productionLow <- data.get("2.8.1")
        productionHigh <- data.get("2.8.2")
      } yield {
        Record(Usage(usageLow, usageHigh), Production(productionLow, productionHigh))
      })
      case (Builder(data, _), valuePattern(field, kilo, watt)) => Builder(data + (field -> (kilo + watt).toInt), None)
      case (builder, line) => builder.copy(record = None)
    }
    .collect {
      case Builder(_, Some(record)) => record
    }
    .runForeach(r => println(s"${r.usage.low},${r.usage.high},${r.production.low},${r.production.high}"))(materializer)

  done.onComplete(_ => system.terminate())
}
