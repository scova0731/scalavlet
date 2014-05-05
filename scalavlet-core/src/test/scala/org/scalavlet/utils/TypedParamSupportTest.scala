package org.scalavlet.utils

import org.scalavlet.Scalavlet
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalavlet.test.ScalatraTestHelper


class MyScalatraServlet extends Scalavlet {

  get("/render/:aNumber") { q =>
    val intValue:Option[Int] = q.params.getAs[Int]("aNumber")

    <p>Value is {intValue getOrElse(-1)}</p>
  }
}

@RunWith(classOf[JUnitRunner])
class TypedParamSupportTest extends WordSpec with ScalatraTestHelper {

  addServlet(classOf[MyScalatraServlet], "/*")

  "GET /render/ with a Int param" should {
    "render it if the param is effectively an Int"  in {
      get("/render/1000") { response =>
        response.status should equal (200)
        response.body should equal ("<p>Value is 1000</p>")
      }
    }
    "render -1 if the implicit conversion fails" in {
      get("/render/foo") { response =>
        response.status should equal (200)
        response.body should equal ("<p>Value is -1</p>")
      }
    }
  }
}