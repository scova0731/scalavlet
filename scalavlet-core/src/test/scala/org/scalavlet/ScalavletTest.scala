package org.scalavlet

import java.io.File
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ScalavletTestServlet extends Scalavlet {
  get("/") { req =>
    "root"
  }

  get("/this/:test/should/:pass") { req =>
    req.params("test")+req.params("pass")
  }

  get("/xml/:must/:val") { req =>
    <h1>{ req.params("must")+req.params("val") }</h1>
  }

  post("/post/test") { req =>
    req.params.get("posted_value") match {
      case None => "posted_value is null"
      case Some(s) => s
    }
  }

  post("/post/:test/val") { req =>
    req.params("posted_value")+req.params("test")
  }

  get("/no_content") { req =>
    NoContent()
  }

  get("/return-int") { req =>
    403
  }

//  get("/redirect") {
//    session("halted") = "halted"
//    redirect("/redirected")
//    session("halted") = "did not halt"
//  }

  get("/redirected") { implicit req =>
    session("halted")
  }

  get("/print_referrer") { req =>
    req.referrer getOrElse "NONE"
  }

  get("/binary/test") { req =>
    "test".getBytes
  }

  get("/file") { req =>
    new File(req.params("filename"))
  }

  get("/returns-unit") { req =>
    ()
  }

  get("/trailing-slash-is-optional/?") { req =>
    "matched trailing slash route"
  }

  get("/people/") { req =>
    "people"
  }

  get("/people/:person") { req =>
    req.params.getOrElse("person", "<no person>")
  }

//  get("/init-param/:name") {
//    initParameter(params("name")).toString
//  }
}