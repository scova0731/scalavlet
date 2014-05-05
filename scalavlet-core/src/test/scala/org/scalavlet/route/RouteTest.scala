/*
 * Most of these test cases are ported from http://github.com/sinatra/sinatra/tree master/test/routing_test.rb
 */
package org.scalavlet.route

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalavlet.Scalavlet
import org.scalatest.FunSuite
import scala.io.Source
import org.scalavlet.test.ScalatraTestHelper

class RouteTestServlet extends Scalavlet {
  get("/foo") { req =>
    "matched simple string route"
  }

//  get2(params.getOrElse("booleanTest", "false") == "true") { req =>
//    "matched boolean route"
//  }
//  get2 {
//    case req if req.params.getOrElse("booleanTest", "false") == "true") =>
//      "matched boolean route"
//  }

  get("/optional/?:foo?/?:bar?") { req =>
    (for (key <- List("foo", "bar") if req.params.isDefinedAt(key)) yield key + "=" + req.params(key)).mkString(";")
  }

  get("/optional-ext.?:ext?") { req =>
    (for (key <- List("ext") if req.params.isDefinedAt(key)) yield key + "=" + req.params(key)).mkString(";")
  }

  get("/single-splat/*") { req =>
    req.multiParams.getOrElse("splat", Seq.empty).mkString(":")
  }

  get("/mixing-multiple-splats/*/foo/*/*") { req =>
    req.multiParams.getOrElse("splat", Seq.empty).mkString(":")
  }

  get("/mix-named-and-splat-params/:foo/*") { req =>
    req.params("foo") + ":" + req.params("splat")
  }

  get("/dot-in-named-param/:foo/:bar") { req =>
    req.params("foo")
  }

  get("/dot-outside-named-param/:file.:ext") { req =>
    //List("file", "ext") foreach { x => response.setHeader(x, req.params(x)) }
    respond.ok(headers = List("file", "ext").map(x => (x, req.params(x))).toMap)
  }

  get("/literal.dot.in.path") { req =>
    "matched literal dot"
  }

  get("/test$") { req =>
    "test$"
  }

  get("/te+st") { req =>
    "te+st"
  }

  get("/test(bar)") { req =>
    "test(bar)"
  }

  get("/conditional") { req =>
    "false"
  }

//  get2("/conditional", params.getOrElse("condition", "false") == "true") {
//    "true"
//  }

  get("""^\/fo(.*)/ba(.*)""".r) { req =>
    req.multiParams.getOrElse("captures", Seq.empty) mkString (":")
  }

  get("""^/foo.../bar$""".r) { req =>
    "regex match"
  }

  get("""/reg(ular)?-ex(pression)?""".r) { req =>
    "regex: false"
  }

//  get2("""/reg(ular)?-ex(pression)?""".r, params.getOrElse("condition", "false") == "true") {
//    "regex: true"
//  }

  post() { q =>
    "I match any post!"
  }

  get("/nothing", false) { req =>
    "shouldn't return"
  }

  get("/fail", false, new RouteMatcher {
    def apply(requestPath: String) = { throw new RuntimeException("shouldn't execute"); None }
  }) { q =>
    "shouldn't return"
  }

  get("/encoded-uri-test/:name") { req =>
    println(req.params)
    println(req.params("name"))
  }

  get("/encoded-uri/:name") { req =>
    req.params("name")
  }

  get("/encoded-uri-2/中国话不用彁字。") { req =>
    "中国话不用彁字。"
  }

  get("/encoded-uri-3/%C3%B6") { req =>
    "ö"
  }
  
  get("/semicolon/?") { req =>
    "semicolon"
  }

  get("/semicolon/document") { req =>
    "document"
  }
}

@RunWith(classOf[JUnitRunner])
class RouteTest extends FunSuite with ScalatraTestHelper {
  mount(classOf[RouteTestServlet], "/*")

  mount(new Scalavlet {
    get("/") { q => "root" }
  }, "/subcontext/*")

  test("routes can be a simple string") {
    get("/foo") { res =>
      res.body should equal ("matched simple string route")
    }
  }

//  test("routes can be a boolean expression") {
//    get2("/whatever", "booleanTest" -> "true") { res =>
//      res.body should equal ("matched boolean route")
//    }
//  }

  test("supports optional named params") {
    get("/optional/hello/world") { res =>
      res.body should equal ("foo=hello;bar=world")
    }

    get("/optional/hello") { res =>
      res.body should equal ("foo=hello")
    }

    get("/optional") { res =>
      res.body should equal ("")
    }

    get("/optional-ext.json") { res =>
      res.body should equal ("ext=json")
    }

    get("/optional-ext") { res =>
      res.body should equal ("")
    }
  }

  test("supports single splat params") {
    get("/single-splat/foo") { response =>
      response.body should equal ("foo")
    }

    get("/single-splat/foo/bar/baz") { response =>
      response.body should equal ("foo/bar/baz")
    }
  }

  test("supports mixing multiple splat params") {
    get("/mixing-multiple-splats/bar/foo/bling/baz/boom") {response =>
      response.body should equal ("bar:bling:baz/boom")
    }
  }

  test("supports mixing named and splat params") {
    get("/mix-named-and-splat-params/foo/bar/baz") { response =>
      response.body should equal ("foo:bar/baz")
    }
  }

  test("matches a dot ('.') as part of a named param") {
    get("/dot-in-named-param/user@example.com/name") { response =>
      response.body should equal ("user@example.com")
    }
  }

  test("matches a literal dot ('.') outside of named params") {
    get("/dot-outside-named-param/pony.jpg") { response =>
      response.header("file") should equal ("pony")
      response.header("ext") should equal ("jpg")
    }
  }

  test("literally matches . in paths") {
    get("/literal.dot.in.path") { response =>
      response.body should equal ("matched literal dot")
    }
  }

  test("literally matches $ in paths") {
    get("/test$") { response =>
      response.body should equal ("test$")
    }
  }

  test("literally matches + in paths") {
    get("/te+st") { response =>
      response.body should equal ("te+st")
    }
  }

  test("literally matches () in paths") {
    get("/test(bar)") { response =>
      response.body should equal ("test(bar)")
    }
  }
  
  test("literally matches ; in paths") {
    get("/foo;123") { response =>
      response.status should equal (200)
    }
  }

//  test("supports conditional path routes") {
//    get2("/conditional", "condition" -> "true") { response =>
//      response.body should equal ("true")
//    }
//
//    get2("/conditional") { response =>
//      response.body should equal ("false")
//    }
//  }

  test("supports regular expressions") {
    get("/foooom/bar") { response =>
      response.body should equal ("regex match")
    }
  }

  test("makes regular expression captures available in params(\"captures\")") {
    get("/foorooomma/baf") { response =>
      response.body should equal ("orooomma:f")
    }
  }

//  test("supports conditional regex routes") {
//    get2("/regular-expression", "condition" -> "true") { response =>
//      response.body should equal ("regex: true")
//    }
//
//    get2("/regular-expression", "condition" -> "false") { response =>
//      response.body should equal ("regex: false")
//    }
//  }

  test("a route with no matchers matches all requests to that method") {
    post("/an-arbitrary-path") { response =>
      response.body should equal ("I match any post!")
    }
  }

  test("matchers should not execute if one before it fails") {
    get("/fail") { response =>
      response.body should not include ("shouldn't return")
    }
  }

  test("trailing slash is optional in a subcontext-mapped servlet") {
    get("/subcontext") { response =>
      response.body should equal ("root")
    }

    get("/subcontext/") { response =>
      response.body should equal ("root")
    }
  }

  test("handles encoded characters in uri") {
    get("/encoded-uri/ac/dc") { response =>
      response.status should equal (405)
    }

    get("/encoded-uri/ac%2Fdc") { response =>
      response.status should equal (200)
      response.body should equal ("ac/dc")
    }

    get("/encoded-uri/%23toc") { response =>
      response.status should equal (200)
      response.body should equal ("#toc")
    }

    get("/encoded-uri/%3Fquery") { response =>
      response.status should equal (200)
      response.body should equal ("?query")
    }

    //This part fails when it's GZipped
    get("/encoded-uri/Fu%C3%9Fg%C3%A4nger%C3%BCberg%C3%A4nge%2F%3F%23") { response =>
      response.status should equal (200)
      //This part doesn't work in Maven test (it's OK on IntelliJ)
      //response.body should equal ("Fußgängerübergänge/?#")
    }

    get("/encoded-uri/ö%C3%B6%25C3%25B6") { response =>
      response.status should equal (200)
      //This part doesn't work in Maven test (it's OK on IntelliJ)
      //response.body should equal ("öö%C3%B6")
    }

    get("/encoded-uri-2/中国话不用彁字。") { response =>
      response.status should equal (200)
    }

    get("/encoded-uri-2/%E4%B8%AD%E5%9B%BD%E8%AF%9D%E4%B8%8D%E7%94%A8%E5%BD%81%E5%AD%97%E3%80%82") { response =>
      response.status should equal (200)
    }

    // mixing encoded with decoded characters
    get("/encoded-uri-2/中国%E8%AF%9D%E4%B8%8D%E7%94%A8%E5%BD%81%E5%AD%97%E3%80%82") { response =>
      response.status should equal (200)
    }

    get("/encoded-uri-3/%25C3%25B6") { response =>
      response.status should equal (200)
    }
  }
  
   test("should chop off uri starting from semicolon") {
    get("/semicolon;jessionid=9328475932475") { response =>
      response.status should equal(200)
      response.body should equal("semicolon")
    }

    get("/semicolon/;jessionid=9328475932475") { response =>
      response.status should equal(200)
      response.body should equal("semicolon")
    }

    get("/semicolon/document;version=3/section/2") { response =>
      response.status should equal(200)
      response.body should equal("document")
    }
  }

}
