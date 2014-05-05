//package org.scalavlet.route
//
//import scala.concurrent.ops._
//import org.scalatest.{WordSpec, Matchers, FlatSpec}
//import org.scalavlet.{HttpMethod, ScalavletServlet, ScalatraSuite}
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//
//class RouteConcurrencyServlet extends ScalavletServlet {
//  for {
//    i <- 0 until 250
//    x = future { get(false) { "/"} }
//  } x()
//
//  val postRoutes = for {
//    i <- 0 until 250
//    x = future { post(false) { "/"} }
//  } yield x()
//
//  for {
//    route <- postRoutes.take(250)
//    x = future { post(false) {}; post(false) {}} // add some more routes while we're removing
//    y = future { post("POST", route) }
//  } (x(), y())
//
//  get("/count/:method") {
//    routes(HttpMethod(params("method"))).size.toString
//  }
//}
//
//@RunWith(classOf[JUnitRunner])
//class RouteConcurrencySpec extends WordSpec with ScalatraSuite with Matchers {
//  addServlet(classOf[RouteConcurrencyServlet], "/*")
//
//  "A scalatra kernel " should {
//    "support adding routes concurrently" in {
//      get("/count/get") {
//        body should equal ("251") // the 500 we added in the future, plus this count route
//      }
//    }
//
//    "support removing routes concurrently with adding routes" in {
//      get("/count/post") {
//        body should equal ("500")
//      }
//    }
//  }
//}
