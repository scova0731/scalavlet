package org

import org.scalavlet.utils.{MultiParams, Params}

import scala.annotation.implicitNotFound
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.util.Try
import org.scalavlet.Request


package object scalavlet
//  extends Control // make halt and pass visible to helpers outside the DSL
//  with DefaultValues // make defaults visible
{


  /**
   * Shorthand of HttpServletRequest
   */
  type SvRequest = HttpServletRequest


  /**
   * Shorthand of HttpServletResponse
   */
  type SvResponse = HttpServletResponse


//  /**
//   * Parameters that returns Seq[String]
//   */
//  type MultiParams = MultiParams


//  /**
//   * Parameters that returns String
//   */
//  type Params = Params


  type Action = () => Any
  type Action2 = Request => Any
  type ResponseAction = Response => Any


  //Base
  type ErrorHandler = PartialFunction[Throwable, Any]


  //Base
  type ContentTypeInferrer = PartialFunction[Any, String]


  //Base
  type RenderPipeline = PartialFunction[Any, Any]


  type Callback = Try[Any] => Unit


  //  type CoreStackNoFlash = CorsSupport with FutureSupport
//  type CoreStackNoFlashWithCsrf = CoreStackNoFlash with CsrfTokenSupport
//  type CoreStackNoFlashWithXsrf = CoreStackNoFlash with XsrfTokenSupport
//
//  type FuturesAndFlashStack = FutureSupport with FlashMapSupport
//  type FuturesAndFlashStackWithCsrf = FuturesAndFlashStack with CsrfTokenSupport
//  type FuturesAndFlashStackWithXsrf = FuturesAndFlashStack with XsrfTokenSupport
//
//  type CoreStack = CorsSupport with FutureSupport with FlashMapSupport
//  type CoreStackWithCsrf = CoreStack with CsrfTokenSupport
//  type CoreStackWithXsrf = CoreStack with XsrfTokenSupport
//
//  type FullCoreStack = CoreStack with FileUploadSupport
//  type FileUploadStack = FutureSupport with FlashMapSupport with FileUploadSupport
//


  /**
   * Structural type for the various Servlet API objects that have attributes.
   * These include ServletContext, HttpSession, and ServletRequest.
   */
  private[scalavlet] type Attributes = {
    def getAttribute(name: String): AnyRef
    def getAttributeNames(): java.util.Enumeration[String]
    def setAttribute(name: String, value: AnyRef): Unit
    def removeAttribute(name: String): Unit
  }

  @implicitNotFound(msg = "Cannot find a TypeConverter type class from ${S} to ${T}")
  trait TypeConverter[S, T] { def apply(s: S): Option[T] }


}
