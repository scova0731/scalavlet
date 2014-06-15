package org

import scala.annotation.implicitNotFound
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.util.Try
import org.scalavlet.utils.Params


package object scalavlet {


  /**
   * Shorthand of HttpServletRequest
   */
  type SvRequest = HttpServletRequest


  /**
   * Shorthand of HttpServletResponse
   */
  type SvResponse = HttpServletResponse


  type Action = () => Any
  type Action2 = Request => Any
  type Action3 = (Request, Params) => Any
  type ResponseAction = Response => Any


  type Callback = Try[Any] => Unit


  @implicitNotFound(msg = "Cannot find a TypeConverter type class from ${S} to ${T}")
  trait TypeConverter[S, T] { def apply(s: S): Option[T] }


}
