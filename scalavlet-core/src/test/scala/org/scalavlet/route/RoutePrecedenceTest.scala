package org.scalavlet.route

import org.scalavlet.Scalavlet
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalavlet.test.ScalatraTestHelper


class RoutePrecedenceTestBaseServlet extends Scalavlet {
  get("/override-route") { q =>
    "base"
  }
}

class RoutePrecedenceTestChildServlet extends RoutePrecedenceTestBaseServlet {
  get("/override-route") { q =>
    "child"
  }

  get("/hide-route") { q =>
    println("hidden by later route")
    "hidden by later route"
  }

  get("/hide-route") { q =>
    println("visible")
    "visible"
  }

//  get2("/pass") { q =>
//    render.ok("3")
//  }
//
//  get2("/pass") { q =>
//    render.ok("This is not written")
//    pass()
//    throw new RuntimeException("This is not thrown")
//  }

//  get2("/pass-to-not-found") { q =>
//    render.ok("a")
//    pass()
//    render.ok("b")
//  }

  get("/do-not-pass") { q =>
    respond.ok("This is not called")
  }

  get("/do-not-pass") { q =>
    respond.ok("1")
  }

//  notFound {
//    response.writer.write("c")
//  }
}

@RunWith(classOf[JUnitRunner])
class RoutePrecedenceTest extends FunSuite with ScalatraTestHelper  {
  addServlet(classOf[RoutePrecedenceTestChildServlet], "/*")

  test("Routes in child should override routes in base") {
    get("/override-route") { response =>
      response.body should equal ("child")
    }
  }

  test("Routes declared later in the same class take precedence") {
    /*
     * This is the opposite of Sinatra, where the earlier route wins.  But to do otherwise, while also letting child
     * classes override base classes' routes, proves to be difficult in an internal Scala DSL.  Sorry, Sinatra users.
     */
    get("/hide-route") { response =>
      "visible"
    }
  }

//  test("pass immediately passes to next matching route") {
//    get2("/pass") { response =>
//      response.body should equal ("3")
//    }
//  }

//  test("pass invokes notFound action if no more matching routes") {
//    get2("/pass-to-not-found") { response =>
//      //Because not_found is not implemented
//      response.body should equal ("404 Not Found")
//      //body should equal ("ac")
//    }
//  }

  test("does not keep executing routes without pass") {
    get("/do-not-pass") { response =>
      response.body should equal ("1")
    }
  }
}
