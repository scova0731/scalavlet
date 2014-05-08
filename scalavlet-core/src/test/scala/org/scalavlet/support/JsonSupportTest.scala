package org.scalavlet.support

import org.scalatest.FunSuite
import org.scalavlet.test.ScalatraTestHelper
import org.scalavlet.Scalavlet
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

@RunWith(classOf[JUnitRunner])
class JsonSupportTest extends FunSuite with ScalatraTestHelper {

  case class Book(title:String, id:Int) {
    override def toString = {
      s"title=$title, id=$id"
    }
  }

  case class Amazon(name:String, books:List[Book]) {
    override def toString = {
      s"name=$name, books=[${books.mkString(", ")}]"
    }
  }

  mount(new Scalavlet {
    post("/book-raw") { q =>
      val body = q.parseJsonBody[Book]
      body
    }

    post("/book") { q =>
      val body = q.parseJsonBody[Book]
      respond.json(body)
    }

    post("/amazon") { q =>
      val body = q.parseJsonBody[Amazon]
      respond.json(body)
    }
  }, "/*")


  test("parse directly") {
    implicit val formats = DefaultFormats
    val parsed = parse("""{"title":"Hamlet","id":12}""")
    val book = parsed.extract[Book]
    book.title should equal ("Hamlet")
  }

  test("parse simple case class") {
    post("/book-raw", """{"title":"Hamlet","id":12}""") { response =>
      response.body should equal ("""title=Hamlet, id=12""")
    }
  }

  test("parse simple case class and convert back to JSON again") {
    post("/book", """{"title":"Hamlet","id":12}""") { response =>
      response.body should equal ("""{"title":"Hamlet","id":12}""")
    }
  }


//  test("parse parent-child case classes") {
//    post("/json", "") { response =>
//      response.body should equal ("")
//    }
//  }
}
