package nl.dvandam.meterkast
import Telegram._

case class Builder(recordedData: Map[String, String], record: Option[Record] = None) {
  def build: Builder = Builder(for {
    usageLow <- recordedData.get(powerConsumptionLow)
    usageHigh <- recordedData.get(powerConsumptionHigh)
    productionLow <- recordedData.get(powerProductionLow)
    productionHigh <- recordedData.get(powerProductionHigh)
    gasMeasureTime <- recordedData.get(gasMeasurementTime)
    gasUsage <- recordedData.get(gasUsage)
  } yield {
    Record(
      PowerConsumption(usageLow.toInt, usageHigh.toInt),
      PowerProduction(productionLow.toInt, productionHigh.toInt),
      GasUsage(gasUsage.toDouble, gasMeasureTime)
    )
  })
}
object Builder {
  val empty = Builder(Map.empty, None)
  def apply(record: Option[Record]): Builder = Builder(Map.empty, record)
}