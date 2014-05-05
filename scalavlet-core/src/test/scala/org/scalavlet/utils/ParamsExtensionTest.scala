package org.scalavlet.utils

import java.util.Date
import java.text.SimpleDateFormat
import org.scalatest.{WordSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParamsExtensionTest extends WordSpec with Matchers {


  case class FakeParams(params: Map[String, String])
    extends Params(params.map(e => (e._1, Seq(e._2))))


  "Scalavlet 'Params pimping'" should {

    "add a getAs[T] method to Scalatra Params that returns Option[T]" in {

      val params: Params = FakeParams(Map("a" -> "1", "b" -> "", "c" -> null))

      params.getAsInt("a").get should equal (1)

      params.getAsInt("b") should equal (None)
      params.getAsInt("c") should equal (None)

      params.getAsInt("unexistent") should equal (None)
    }

    "add a getAs[Date] method to Scalatra Params that returns Option[Date]" in {

      val (format, dateAsText) = ("dd/MM/yyyy", "9/11/2001")

      val params: Params = FakeParams(Map("TwinTowers" -> dateAsText))

      val expectedDate = new SimpleDateFormat(format).parse(dateAsText)

      params.getAs[Date]("TwinTowers" -> format) should equal (Some(expectedDate))

    }

    "return None if a conversion is invalid" in {
      val params: Params = FakeParams(Map("a" -> "hello world"))
      params.getAsInt("a") should equal (None)
    }

    case class Bogus(name: String)

    "implicitly find TypeConverter(s) for a custom type" in {

      //implicit val bogusConverter: TypeConverter[String, Bogus] = (s: String) => Some(Bogus(s))

      val params: Params = FakeParams(Map("a" -> "buffybuffy"))

      //params.getAs[Bogus]("a") should equal (Some("buffybuffy"))

      //params.getAs[Bogus]("a").get aka "The bogus value" must_== Bogus("buffybuffy")

    }

  }

  "Scalavlet 'MultiParams' pimping" should {

    "add a getAs[T] method" in {
      val multiParams: MultiParams = Map("CODES" -> List("1", "2").toSeq, "invalids" -> List("a", "b"))
      multiParams.getAsInt("CODES").get should equal (List(1, 2))
    }

    "return None for unexistent parameters" in {
      val multiParams: MultiParams = Map("invalids" -> List("1", "a", "2"))
      multiParams.getAsInt("blah") should equal (None)
    }


    "return None if some conversion is invalid" in {
      val multiParams: MultiParams = Map("invalids" -> List("1", "a", "2"))
      multiParams.getAsInt("invalids") should equal (None)
    }

    "return None if all conversions are invalid" in {
      val multiParams: MultiParams = Map("invalids" -> List("a", "b"))
      multiParams.getAsInt("invalids") should equal (None)
    }

//    "add a getAs[Date] method" in {
//
//      val (format, datesAsText) = ("dd/MM/yyyy", List("20/12/2012", "10/02/2001"))
//
//      val multiParams: ParamMap = Map("DATES" -> datesAsText.toSeq)
//
//      val expectedDates = datesAsText.map {
//        new SimpleDateFormat(format).parse(_)
//      }
//
//      //multiParams.getAs[Date]("DATES" -> format) should equal beSome[Seq[Date]]
//      //multiParams.getAs[Date]("DATES" -> format).get should equal (expectedDates)
//    }
  }
}


