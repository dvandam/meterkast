package nl.dvandam.meterkast

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContextExecutor, Future}

object Meterkast extends App {
  implicit val system: ActorSystem = ActorSystem("meterkast")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val kwhPattern = """1-0:([12].8.[12]).(\d+).(\d+).kWh.*""".r
  val gasMeasureTimePattern = """0-1:24.3.0.(\d+).*""".r
  val gasUsagePattern = """.(\d+\.\d+).""".r

  val done: Future[Done] = Source.fromIterator { () => StdInReader }
    .scan(Builder(Map.empty, None)) {
      case (_, "/ISk5\\2MT382-1003") => Builder(Map.empty, None) // start
      case (Builder(data, _), "!") => Builder(Map.empty, for { // end
        usageLow <- data.get("1.8.1")
        usageHigh <- data.get("1.8.2")
        productionLow <- data.get("2.8.1")
        productionHigh <- data.get("2.8.2")
        gasMeasureTime <- data.get("24.3.0")
        gasUsage <- data.get("gasUsage")
      } yield {
        Record(
          PowerConsumption(usageLow.toInt, usageHigh.toInt),
          PowerProduction(productionLow.toInt, productionHigh.toInt),
          GasUsage(gasUsage.toDouble, gasMeasureTime))
      })
      case (Builder(data, _), kwhPattern(field, kilo, watt)) => Builder(data + (field -> (kilo + watt)), None)
      case (Builder(data, _), gasMeasureTimePattern(time)) => Builder(data + ("24.3.0" -> time), None)
      case (Builder(data, _), gasUsagePattern(usage)) => Builder(data + ("gasUsage" -> usage), None)
      case (builder, _) => builder.copy(record = None)
    }
    .collect {
      case Builder(_, Some(record)) => record
    }
    .map { r =>
      s"${r.consumption.low},${r.consumption.high}," +
      s"${r.production.low},${r.production.high}," +
      s"${r.gasUsage.measureTime},${r.gasUsage.usage}"
    }
    .runForeach(println)(materializer)

  done.onComplete(_ => system.terminate())
}
