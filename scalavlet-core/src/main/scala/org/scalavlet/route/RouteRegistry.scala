package org.scalavlet.route

import org.scalavlet.{Get, Head, HttpMethod}
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.annotation.tailrec
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._

/**
 * RouteRegistry keeps routes information, usually in each ScalavletServlet
 * Not context ? but servlet ?
 */
class RouteRegistry {

//  private[this] val _statusRoutes: ConcurrentMap[Int, Route] =
//    new ConcurrentHashMap[Int, Route].asScala

  private[this] val routes: ConcurrentMap[HttpMethod, Seq[Route]] =
    new ConcurrentHashMap[HttpMethod, Seq[Route]].asScala


//  def apply(statusCode: Int): Option[Route] = _statusRoutes.get(statusCode)
//
//  /**
//   * Add a route that explicitly matches one or more response codes.
//   */
//  def addStatusRoute(codes: Range, route: Route): Unit =
//    codes.foreach { code => _statusRoutes.put(code, route) }







  /**
   * Returns the sequence of routes registered for the specified method.
   *
   * HEAD must be identical to GET without a body, so HEAD returns GET's
   * routes.
   */
  def apply(method: HttpMethod): Seq[Route] =
    method match {
      case Head => routes.getOrElse(Head, routes.getOrElse(Get, Vector.empty))
      case m => routes.getOrElse(m, Vector.empty)
    }


  /**
   * Returns a set of methods with a matching route.
   *
   * HEAD must be identical to GET without a body, so GET implies HEAD.
   */
  def matchingMethods(requestPath: String): Set[HttpMethod] =
    matchingMethodsExcept(requestPath) { _ => false }

  /**
   * Returns a set of methods with a matching route minus a specified
   * method.
   *
   * HEAD must be identical to GET without a body, so:
   * - GET implies HEAD
   * - filtering one filters the other
   */
  def matchingMethodsExcept(method: HttpMethod, requestPath: String): Set[HttpMethod] = {
    val p: HttpMethod => Boolean = method match {
      case Get | Head => { m => m == Get || m == Head }
      case _ => { _ == method }
    }
    matchingMethodsExcept(requestPath)(p)
  }

  private def matchingMethodsExcept(requestPath: String)(p: HttpMethod => Boolean) = {
    var methods = (routes filter { kv =>
      val method = kv._1
      val routes = kv._2
      !p(method) && (routes exists (_.find(requestPath).isDefined))
    }).keys.toSet
    if (methods.contains(Get))
      methods += Head
    methods
  }


  /**
   * Prepends a route to the method's route sequence.
   */
  def prependRoute(method: HttpMethod, route: Route): Unit =
    updateRoutes(method, route +: _)

  
  /**
   * Removes a route from the method's route sequence.
   */
  def removeRoute(method: HttpMethod, route: Route): Unit =
    updateRoutes(method, _ filterNot (_ == route))

  
  /**
   *
   */
  @tailrec private def updateRoutes(method: HttpMethod, f: (Seq[Route] => Seq[Route])): Unit = {
    //If the method is not registered, add empty vector
    if (routes.putIfAbsent(method, f(Vector.empty)).isDefined) {
      //If present, just do f()
      val oldRoutes = routes(method)
      if (!routes.replace(method, oldRoutes, f(oldRoutes)))
        updateRoutes(method, f)
    }
  }

  /**
   * List of entry points, made of all route matchers
   */
  def entryPoints: Seq[String] =
    (for {
      (method, routes) <- routes
      route <- routes
    } yield method + " " + route).toSeq sortWith (_ < _)

  def methodRoutes = routes.clone()




  override def toString: String = entryPoints mkString ", "
}
