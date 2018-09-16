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

  case class Builder(recordedData: Map[String, String], record: Option[Record])
}
