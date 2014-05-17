package org.scalavlet

import org.scalavlet.test.ScalatraTestHelper

import org.junit.runner.RunWith

import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

class RequestBodyTestServlet extends Scalavlet {
  post("/request-body") { q =>
    val body = q.body
    val body2 = q.body
    respond.ok(body = q.body, headers = Map("X-Idempotent" -> (body == body2).toString))
  }

  get("/not-implemented") { q =>
    NotImplemented
  }
}

@RunWith(classOf[JUnitRunner])
class RequestBodyTest extends FunSuite with ScalatraTestHelper {
  addServlet(classOf[RequestBodyTestServlet], "/*")

  test("can read request body") {
    post("/request-body", "My cat's breath smells like cat food!") { response =>
      response.body should equal ("My cat's breath smells like cat food!")
    }
  }

  test("request body is idempotent") {
    post("/request-body", "Miss Hoover, I glued my head to my shoulder.") { response =>
      response.header("X-Idempotent") should equal ("true")
    }
  }

  test("ant other status") {
    get("/not-implemented") { response =>
      //TODO configure response HTML body
      //response.body should equal ("")
      response.status should equal (501)
    }
  }

}
