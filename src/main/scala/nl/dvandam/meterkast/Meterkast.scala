package nl.dvandam.meterkast

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import Telegram._

import scala.concurrent.{ExecutionContextExecutor, Future}

object Meterkast extends App {
  implicit val system: ActorSystem = ActorSystem("meterkast")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher


  val done: Future[Done] = Source.fromIterator { () => StdInReader }
    .scan(Builder.empty) {
      case (_, Telegram.start) => Builder.empty
      case (builder, Telegram.end) => builder.build
      case (Builder(data, _), kwhPattern(field, kilo, watt)) => Builder(data + (field -> (kilo + watt)))
      case (Builder(data, _), gasMeasureTimePattern(time)) => Builder(data + (gasMeasurementTime -> time))
      case (Builder(data, _), gasUsagePattern(usage)) => Builder(data + (gasUsage -> usage))
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
