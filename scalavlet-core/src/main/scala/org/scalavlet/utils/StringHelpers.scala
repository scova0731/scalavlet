package org.scalavlet.utils

import org.scalavlet.richer.ImplicitRichers
import org.scalavlet.Responser

import java.io.File


object StringHelpers extends ImplicitRichers {

  /**
   * A partial function to infer the content type from the action result.
   *
   * from org.scalatra.ScalatraBase
   *
   * @return
   * $ - "text/plain" for String
   * $ - "application/octet-stream" for a byte array
   * $ - "text/html" for any other result
   */
  def contentTypeInferrer: PartialFunction[Any, String] = {
    case s: String =>
      "text/plain"

    case bytes: Array[Byte] =>
      MimeTypes(bytes)

    case is: java.io.InputStream =>
      MimeTypes(is)

    case file: File =>
      MimeTypes(file)

    case actionResult: Responser =>
      actionResult.headers.find {
        case (name, value) => name equalsIgnoreCase "CONTENT-TYPE"
      }.getOrElse(("Content-Type", contentTypeInferrer(actionResult.body)))._2

    case _ => "text/html"
  }


  def ensureContextPathsStripped(servletPath: Option[String], path: String, contextPath: String): String =
    ensureServletPathStripped(servletPath, ensureContextPathStripped(path, contextPath))


  def ensureServletPathStripped(servletPath: Option[String], path: String): String = {
    val sp = ensureSlash(servletPath getOrElse "")
    val np = if (path.startsWith(sp + "/")) path.substring(sp.length) else path
    ensureSlash(np)
  }


  def ensureContextPathStripped(path: String, contextPath:String): String = {
    val cp = ensureSlash(contextPath)
    val np = if (path.startsWith(cp + "/")) path.substring(cp.length) else path
    ensureSlash(np)
  }


  def ensureSlash(candidate: String): String = {
    val p = if (candidate.startsWith("/")) candidate else "/" + candidate
    if (p.endsWith("/")) p.dropRight(1) else p
  }
}






