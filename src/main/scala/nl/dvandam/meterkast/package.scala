package nl.dvandam

package object meterkast {
  case class Usage(low: Int, high: Int) {
    lazy val total: Int = low + high
  }
  case class Production(low: Int, high: Int) {
    lazy val total: Int = low + high
  }
  case class Record(usage: Usage, production: Production) {
    lazy val total: Int = usage.total - production.total
  }

  case class Builder(recordedData: Map[String, Int], record: Option[Record])
}
