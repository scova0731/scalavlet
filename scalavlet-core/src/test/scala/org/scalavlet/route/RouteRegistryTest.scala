package org.scalavlet.route

import org.scalatest.{Matchers, FunSuite}
import org.scalavlet.Scalavlet
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

object RouteRegistryTestServlet extends Scalavlet {
  get("/foo") { q => }
  post("/foo/:bar") { q => }
  put("""^/foo.../bar$""".r) { q => }
  get("/nothing", false) { q => }
  get(false) { q =>}

  def renderRouteRegistry: String = routes.toString
}

@RunWith(classOf[JUnitRunner])
class RouteRegistryTest extends FunSuite with Matchers {

  test("route registry string representation contains the entry points") {
    RouteRegistryTestServlet.renderRouteRegistry should equal (List(
      "GET /foo",
      "GET /nothing [Boolean Guard]",
      "GET [Boolean Guard]",
      "POST /foo/:bar",
      "PUT ^/foo.../bar$"
    ) mkString ", ")
  }
}
