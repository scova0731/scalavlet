package org.scalavlet.support

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.reflect.ClassTag


trait JsonSupport {

  def body:String

  def parseJsonBody[A](implicit mf:Manifest[A]):A = {
    implicit val formats = DefaultFormats
    val parsed = parse(body)
    parsed.extract[A]
  }

}
