package nl.dvandam.meterkast

import scala.util.matching.Regex

object Telegram {
  val start: String = """/ISk5\2MT382-1003"""
  val end: String = "!"
  val powerConsumptionLow: String = "1.8.1"
  val powerConsumptionHigh: String = "1.8.2"
  val powerProductionLow: String = "2.8.1"
  val powerProductionHigh: String = "2.8.2"
  val gasMeasurementTime: String = "24.3.0"
  val gasUsage: String = "gasUsage"
  val kwhPattern: Regex = s"""1-0:([12].8.[12]).(\d+).(\d+).kWh.*""".r
  val gasMeasureTimePattern: Regex = """0-1:24.3.0\((\d+)\).*""".r
  val gasUsagePattern: Regex = """.(\d+\.\d+).""".r
}
