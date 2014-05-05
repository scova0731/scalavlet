package org.scalavlet

import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ScalatraServletRequestPathTest extends WordSpec with MustMatchers {

  val servlet = new Scalavlet {}

  "a ScalatraServlet requestPath" should {

    "be extracted properly from encoded url" in {
      servlet.requestPath("/%D1%82%D0%B5%D1%81%D1%82/", 5) must equal("/")
      servlet.requestPath("/%D1%82%D0%B5%D1%81%D1%82/%D1%82%D0%B5%D1%81%D1%82/", 5) must equal("/тест/")
    }

    "be extracted properly from decoded url" in {
      servlet.requestPath("/тест/", 5) must equal("/")
      servlet.requestPath("/тест/тест/", 5) must equal("/тест/")
    }
  }
}
