package org.scalavlet

import org.scalavlet.support.Cookie
import org.scalavlet.richer.ImplicitRichers

import javax.servlet.http.{Cookie => ServletCookie}
import javax.servlet.ServletOutputStream

import scala.collection.JavaConverters._
import scala.collection.mutable
import java.io.PrintWriter

/**
 *
 */
class Response(r: SvResponse) extends ImplicitRichers {

  def underlying: SvResponse = r

  val headers: ResponseHeaders = new ResponseHeaders(r)


  /**
   * Note: the servlet API doesn't remember the reason.  If a custom
   * reason was set, it will be returned incorrectly here,
   */
  def status: Status = Status(r.getStatus)

  
  def setStatus(s:Int):Unit = setStatus(Status(s))

  
  def setStatus(statusLine: Status):Unit =
    if (statusLine.isOk)
      r.setStatus(statusLine.code)
    else
      r.sendError(statusLine.code, statusLine.message)


  def addHeader(name:String, value:String): Unit = 
    r.addHeader(name, value)

  
  def setHeader(name:String, value:String): Unit = 
    r.setHeader(name, value)


  def addCookie(c: Cookie):Unit = {
    val sCookie = new ServletCookie(c.name, c.value)
    if (c.options.domain.nonBlank) sCookie.setDomain(c.options.domain)
    if (c.options.path.nonBlank) sCookie.setPath(c.options.path)
    sCookie.setMaxAge(c.options.maxAge)
    sCookie.setSecure(c.options.secure)
    if (c.options.comment.nonBlank) sCookie.setComment(c.options.comment)
    sCookie.setHttpOnly(c.options.httpOnly)
    sCookie.setVersion(c.options.version)
    r.addCookie(sCookie)
  }


  def characterEncoding: Option[String] =
    Option(r.getCharacterEncoding)


  def setCharacterEncoding(encoding: Option[String]):Unit =
    r.setCharacterEncoding(encoding getOrElse null)


  def encodeURL(url:String):String = r.encodeURL(url)


  def contentType: Option[String] =
    Option(r.getContentType)


  def setContentType(contentType: Option[String]):Unit =
    r.setContentType(contentType getOrElse null)


  def redirect(uri: String):Unit =
    r.sendRedirect(uri)


  def outputStream: ServletOutputStream =
    r.getOutputStream


  def writer: PrintWriter =
    r.getWriter


  def end() {
    r.flushBuffer()
    r.getOutputStream.close()
  }
}


class ResponseHeaders(underlying:SvResponse) extends mutable.Map[String, String] {
  def get(key: String): Option[String] =
    underlying.getHeaders(key) match {
      case xs if xs.isEmpty => None
      case xs => Some(xs.asScala mkString ",")
    }


  def iterator: Iterator[(String, String)] =
    for (name <- underlying.getHeaderNames.asScala.iterator)
    yield (name, underlying.getHeaders(name).asScala mkString ", ")


  def +=(kv: (String, String)): this.type = {
    underlying.setHeader(kv._1, kv._2)
    this
  }


  def -=(key: String): this.type = {
    underlying.setHeader(key, "")
    this
  }
}