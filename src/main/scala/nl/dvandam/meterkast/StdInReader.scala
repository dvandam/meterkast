package nl.dvandam.meterkast

import scala.io.StdIn

object StdInReader extends Iterator[String] {
  var nextLine: Option[String] = None

  override def hasNext: Boolean = {
    nextLine = nextLine.orElse(Option(StdIn.readLine))
    nextLine.isDefined
  }
  override def next: String = {
    val line = nextLine.getOrElse("")
    nextLine = None
    line
  }
}
