package nl.dvandam.meterkast

import scala.io.StdIn

object StdInReader extends Iterator[String] {
  var nextLines: Seq[String] = Seq.empty

  override def hasNext: Boolean = {
    Option(StdIn.readLine()).foreach(line => nextLines = nextLines :+ line)
    nextLines != Nil
  }
  override def next: String = {
    val nextLine: String = nextLines.headOption.getOrElse("")
    nextLines = nextLines.tail
    nextLine
  }
}
