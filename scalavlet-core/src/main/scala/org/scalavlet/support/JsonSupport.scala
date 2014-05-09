package org.scalavlet.support

import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.fasterxml.jackson.core.JsonParseException


/**
 * Support for JSON parsing of request
 */
trait JsonSupport {

  implicit val formats = DefaultFormats


  def body:String


  def jsonBody[A](implicit mf:Manifest[A]):A = {
    val parsed = parse(body)
    parsed.extract[A]
  }
  

  def tryJsonBody[A](implicit mf:Manifest[A]):Either[String, A] = {
    try {
      val parsed = parse(body)
      Right(parsed.extract[A])
    } catch {
      case ex:JsonParseException =>
        Left(ex.getMessage)
    }
  }
}
