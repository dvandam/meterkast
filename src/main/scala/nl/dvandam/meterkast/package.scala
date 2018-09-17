package nl.dvandam

package object meterkast {
  case class PowerConsumption(low: Int, high: Int) {
    lazy val total: Int = low + high
  }
  case class PowerProduction(low: Int, high: Int) {
    lazy val total: Int = low + high
  }
  case class Record(consumption: PowerConsumption, production: PowerProduction, gasUsage: GasUsage) {
    lazy val total: Int = consumption.total - production.total
  }

  case class GasUsage(usage: Double, measureTime: String)

  case class Builder(recordedData: Map[String, String], record: Option[Record] = None) {
    def build: Builder = Builder(for {
      usageLow <- recordedData.get("1.8.1")
      usageHigh <- recordedData.get("1.8.2")
      productionLow <- recordedData.get("2.8.1")
      productionHigh <- recordedData.get("2.8.2")
      gasMeasureTime <- recordedData.get("24.3.0")
      gasUsage <- recordedData.get("gasUsage")
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
}
