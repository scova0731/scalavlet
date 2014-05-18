package org.scalavlet.route

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import org.scalavlet.utils.MultiParams

/**
 * A path pattern optionally matches a request path and extracts path
 * parameters.
 *
 * Derived from Scalatra
 */
case class PathPattern(regex: Regex, captureGroupNames: List[String] = Nil) {
  def genMap(path: String): Option[MultiParams] = { //Option[MultiParams] = {
    // This is a performance hotspot.  Hideous mutatations ahead.
    val m = regex.pattern.matcher(path)
    var multiParams = Map[String, Seq[String]]()
    if (m.matches) {
      var i = 0
      captureGroupNames foreach { name =>
        i += 1
        val value = m.group(i)
        if (value != null) {
          val values = multiParams.getOrElse(name, Vector()) :+ value
          multiParams = multiParams.updated(name, values)
        }
      }
      Some(multiParams)
    } else None
  }
}


/**
 * Parses a string into a path pattern for routing.
 *
 * Derived from Scalatra
 */
trait PathPatternParser {
  def parse(pattern: String): PathPattern
}


/**
 *
 * Derived from Scalatra
 */
trait RegexPathPatternParser extends PathPatternParser with RegexParsers {
  /**
   * This parser gradually builds a regular expression.  Some intermediate
   * strings are not valid regexes, so we wait to compile until the end.
   */
  protected case class PartialPathPattern(regex: String, captureGroupNames: List[String] = Nil)
  {
    def toPathPattern: PathPattern = PathPattern(regex.r, captureGroupNames)

    def +(other: PartialPathPattern): PartialPathPattern = PartialPathPattern(
      this.regex + other.regex,
      this.captureGroupNames ::: other.captureGroupNames
    )
  }
}


/**
 * A Sinatra-compatible route path pattern parser.
 *
 * Derived from Scalatra
 */
class SinatraPathPatternParser extends RegexPathPatternParser {
  def parse(pattern: String): PathPattern =
    parseAll(pathPattern, pattern) match {
      case Success(pathPattern, _) =>
        (PartialPathPattern("^") + pathPattern + PartialPathPattern("$")).toPathPattern
      case _ =>
        throw new IllegalArgumentException("Invalid path pattern: " + pattern)
    }

  private def pathPattern = rep(token) ^^ { _.reduceLeft { _+_ } }

  private def token = splat | namedGroup | literal

  private def splat = "*" ^^^ PartialPathPattern("(.*?)", List("splat"))

  private def namedGroup = ":" ~> """\w+""".r ^^
    { groupName => PartialPathPattern("([^/?#]+)", List(groupName)) }

  private def literal = metaChar | normalChar

  private def metaChar = """[\.\+\(\)\$]""".r ^^
    { c => PartialPathPattern("\\" + c) }

  private def normalChar = ".".r ^^ { c => PartialPathPattern(c) }
}


/**
 *
 * Derived from Scalatra
 */
object SinatraPathPatternParser {
  def parse(pattern: String): PathPattern = new SinatraPathPatternParser().parse(pattern)
}
