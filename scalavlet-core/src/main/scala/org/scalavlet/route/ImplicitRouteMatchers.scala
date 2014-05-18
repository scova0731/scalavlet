package org.scalavlet.route

import scala.util.matching.Regex

// This is added based on the advise of compiler warning
import scala.language.implicitConversions


/**
 * Derived from Scalatra
 */
trait ImplicitRouteMatchers {

  /**
   * Pluggable way to convert a path expression to a route matcher.
   * The default implementation is compatible with Sinatra's route syntax.
   *
   * @param path a path expression
   * @return a route matcher based on `path`
   */
  protected implicit def string2RouteMatcher(path: String): RouteMatcher =
    new SinatraRouteMatcher(path)

  
  /**
   * Path pattern is decoupled from requests.  This adapts the PathPattern to
   * a RouteMatcher by supplying the request path.
   */
  protected implicit def pathPatternParser2RouteMatcher(pattern: PathPattern): RouteMatcher =
    new PathPatternRouteMatcher(pattern)


  /**
   * Converts a regular expression to a route matcher.
   *
   * @param regex the regular expression
   * @return a route matcher based on `regex`
   * @see [[org.scalatra.RegexRouteMatcher]]
   */
  protected implicit def regex2RouteMatcher(regex: Regex): RouteMatcher =
    new RegexRouteMatcher(regex)

  
  /**
   * Converts a boolean expression to a route matcher.
   *
   * @param block a block that evaluates to a boolean
   *
   * @return a route matcher based on `block`.  The route matcher should
   *         return `Some` if the block is true and `None` if the block is false.
   *
   * @see [[org.scalatra.BooleanBlockRouteMatcher]]
   */
  protected implicit def booleanBlock2RouteMatcher(block: => Boolean): RouteMatcher =
    new BooleanBlockRouteMatcher(block)
}
