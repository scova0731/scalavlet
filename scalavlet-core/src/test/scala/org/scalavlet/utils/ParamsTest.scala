package org.scalavlet.utils

import java.util.NoSuchElementException
import io.Codec
import org.scalavlet.Scalavlet
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalavlet.test.ScalatraTestHelper

object ParamsTestServlet {
  val NoSuchElement = "No Such Element"
}

class ParamsTestServlet extends Scalavlet {
  import ParamsTestServlet._

  println("ParamsTestServlet is instantiated")

  get("/multiParams/:key") { q =>
    val keyName = q.params("key")
    q.multiParams.apply(keyName).mkString("[",",","]")
  }

//  get("/multiParams/:key") {
//    multiParams.apply(params("key")).mkString("[",",","]")
//  }

  get("/params/:key") { q =>
    try {
      q.params(q.params("key"))
    }
    catch {
      case _: NoSuchElementException => NoSuchElement
    }
  }

//  get("/symbol/:sym") {
//    params('sym)
//  }
//
//  get("/twoSymbols/:sym1/:sym2") {
//    params('sym1)+" and "+ params('sym2)
//  }

  post("/read-body") { q =>
    "body: " + q.body
  }
}

@RunWith(classOf[JUnitRunner])
class ParamsTest extends FunSuite with ScalatraTestHelper {
  addServlet(classOf[ParamsTestServlet], "/*")

  test("supports multiple parameters") {
    get("/multiParams/numbers", "numbers" -> "one", "numbers" -> "two", "numbers" -> "three") { response =>
      response.body should equal ("[one,two,three]")
    }
  }

//  test("supports multiple parameters with ruby like syntax") {
//    get("/multiParams/numbers_ruby", "numbers_ruby[]" -> "one", "numbers_ruby[]" -> "two", "numbers_ruby[]" -> "three") {
//      body should equal ("[one,two,three]")
//    }
//  }

  test("unknown multiParam returns an empty seq") {
    get("/multiParams/oops") { response =>
      response.status should equal (200)
      response.body should equal ("[]")
    }
  }

  test("params returns first value when multiple values present") {
    get("/params/numbers", "numbers" -> "one", "numbers" -> "two", "numbers" -> "three") { response =>
      response.body should equal ("one")
    }
  }

  test("params on unknown key throws NoSuchElementException") {
    get ("/params/oops") { response =>
      response.body should equal (ParamsTestServlet.NoSuchElement)
    }
  }

//  test("can use symbols as keys for retrieval")  {
//    get("/symbol/hello") {
//      body should equal ("hello")
//    }
//  }
//
//  test("can use symbols multiple times ")  {
//    get("/twoSymbols/hello/world") {
//      body should equal ("hello and world")
//    }
//  }

  test("can read the body of a post") {
    post("/read-body", "hi".getBytes(Codec.UTF8.charSet)) { response =>
      response.body should equal ("body: hi")
    }
  }
}
