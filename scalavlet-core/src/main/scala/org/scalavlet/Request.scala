package org.scalavlet

import org.scalavlet.utils.{StringHelpers, Cacheable, MultiParams, Params}

import scala.collection.immutable.DefaultMap
import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try
import java.net.URI
import java.io.InputStream
import java.util.Locale
import javax.servlet.http.HttpSession

import StringHelpers._
import scala.util.control.Exception._
import org.scalavlet.richer.RichString


class Request(r: SvRequest, response:SvResponse) extends Cacheable {

  def underlying:SvRequest = r

  //Use this carefully
  def underlyingResponse:SvResponse = response

  type CallbackList = List[(Try[Any]) => Unit]

  private var cacheForParams:MultiParams = _
  private var _cachedBody:Option[String] = None

  def addParamsCache(c:MultiParams) = cacheForParams = c
  def paramsCache = cacheForParams


  /**
   * The version of the protocol the client used to send the request.
   * Typically this will be something like "HTTP/1.0"  or "HTTP/1.1" and may
   * be used by the application to determine how to treat any HTTP request
   * headers.
   */
  def serverProtocol: HttpVersion = r.getProtocol match {
    case "HTTP/1.1" => Http11
    case "HTTP/1.0" => Http10
  }


  /**
   * URI from HttpServletRequest
   */
  def uri: URI = new URI(r.getRequestURL.toString)


  /**
   * Http or Https, depending on the request URL.
   */
  def urlScheme: HttpScheme with Product with Serializable = r.getScheme match {
    case "http" => Http
    case "https" => Https
  }

  /**
   * The HTTP request method, such as GET or POST
   */
  def requestMethod: HttpMethod = HttpMethod(r.getMethod)

  /**
   * The remainder of the request URL's "path", designating the virtual
   * "location" of the request's target within the application. This may be
   * an empty string, if the request URL targets the application root and
   * does not have a trailing slash.
   */
  def pathInfo: String = Option(r.getPathInfo) getOrElse ""

  /**
   * The initial portion of the request URL's "path" that corresponds to
   * the application object, so that the application knows its virtual
   * "location". This may be an empty string, if the application corresponds
   * to the "root" of the server.
   */
  def servletPath: String = r.getServletPath


  def contextPath: String = r.getContextPath


  def requestURI: String = r.getRequestURI

  /**
   * The portion of the request URL that follows the ?, if any. May be
   * empty, but is always required!
   */
  def queryString: String = Option(r.getQueryString) getOrElse ""

  
  def isSecure: Boolean = r.isSecure

  def isGZip: Boolean =
    Option(r.getHeader("Accept-Encoding")).getOrElse("").toUpperCase.contains("GZIP")

  def isHttps: Boolean = {
    // also respect load balancer version of the protocol
    val h = header("X-Forwarded-Proto")
    isSecure || (h.isDefined && h.forall(_ equalsIgnoreCase "HTTPS"))
  }


  def session:HttpSession = r.getSession(false)


  def serverAuthority: String =
    if (serverPort == 80 || serverPort == 443) serverName
    else serverName + ":" + serverPort.toString


  def buildBaseUrl: String = {
    "%s://%s".format(
      if (needsHttps || isHttps) "https" else "http",
      serverAuthority
    )
  }


  //TODO get this via Configuration
  def needsHttps: Boolean = allCatch.withApply(_ => false) {
    //servletContext.getInitParameter(ForceHttpsKey).blankOption.map(_.toBoolean) getOrElse
    false
  }


  /**
   * A Map of the parameters of this request. Parameters are contained in
   * the query string or posted form data.
   */
  def multiParams: MultiParams = {
    // At the very least in jetty 8 we see problems under load related to this
    val origParams = if (new RichString(r.getQueryString).nonBlank && r.getParameterMap.isEmpty) {
      val qs = rl.MapQueryString.parseString(r.getQueryString)
      val bd = if (!HttpMethod(r.getMethod).isSafe && r.getHeader("Content-Type").
        equalsIgnoreCase("APPLICATION/X-WWW-FORM-URLENCODED")) {
          rl.MapQueryString.parseString(body)
      } else Map.empty
      qs ++ bd
    } else
      r.getParameterMap.asScala.toMap.transform { (k, v) => v: Seq[String] }

    //TODO Why is this null sometimes ?
    if (paramsCache == null)
      origParams
    else
      origParams ++ paramsCache
  }

  def multiParams(key: String): Seq[String] = multiParams.apply(key)


  def params = Params(multiParams)


  /**
   * A map of headers.  Multiple header values are separated by a ','
   * character.  The keys of this map are case-insensitive.
   */
  //TODO visit later
  object headers extends DefaultMap[String, String] {
    def get(name: String): Option[String] = Option(r.getHeader(name))

    private[scalavlet] def getMulti(key: String): Seq[String] =
      get(key).map(_.split(",").toSeq.map(_.trim)).getOrElse(Seq.empty)

    def iterator: Iterator[(String, String)] =
      r.getHeaderNames.asScala map { name => (name, r.getHeader(name)) }
  }

  def header(name: String): Option[String] = Option(r.getHeader(name))

  /**
   * Returns the name of the character encoding of the body, or None if no
   * character encoding is specified.
   */
  def characterEncoding: Option[String] =
    Option(r.getCharacterEncoding)

  /**
   * Set the passed encoding to the wrapped HttpServletRequest
   * @param encoding
   */
  def setCharacterEncoding(encoding: Option[String]) {
    r.setCharacterEncoding(encoding getOrElse null)
  }

  /**
   * The content of the Content-Type header, or None if absent.
   */
  def contentType: Option[String] =
    Option(r.getContentType)

  /**
   * Returns the length, in bytes, of the body, or None if not known.
   */
  def contentLength: Option[Long] = r.getContentLength match {
    case -1 => None
    case length => Some(length)
  }

  /**
   * When combined with scriptName, pathInfo, and serverPort, can be used to
   * complete the URL.  Note, however, that the "Host" header, if present,
   * should be used in preference to serverName for reconstructing the request
   * URL.
   */
  def serverName = r.getServerName

  /**
   * When combined with scriptName, pathInfo, and serverName, can be used to
   * complete the URL.  See serverName for more details.
   */
  def serverPort = r.getServerPort

  /**
   * Optionally returns the HTTP referrer.
   *
   * @return the `Referer` header, or None if not set
   */
  def referrer: Option[String] = r.getHeader("Referer") match {
    case s: String => Some(s)
    case null => None
  }

  /**
   * Caches and returns the body of the response.  The method is idempotent
   * for any given request.  The result is cached in memory regardless of size,
   * so be careful.  Calling this method consumes the request's input stream.
   *
   * @return the message body as a string according to the request's encoding
   * (defult ISO-8859-1).
   *
   * MEMO
   *   Since this consumes the input stream, it should be cached.
   */
  def body:String = {
    _cachedBody getOrElse {
      val encoding = r.getCharacterEncoding
      val enc =
        if(encoding == null || encoding.trim.length == 0)
          "ISO-8859-1"
        else
          encoding

      val body = Source.fromInputStream(r.getInputStream, enc).mkString
      _cachedBody = Some(body)
      body
    }
  }



  //  private def cachedBody: Option[String] =
//    get(cachedBodyKey).flatMap(_.asInstanceOf[String].blankOption)

  /**
   * Returns true if the request is an AJAX request
   */
  def isAjax: Boolean = r.getHeader("X-Requested-With") != null

  /**
   * Returns true if the request's method is not "safe" per RFC 2616.
   */
  def isWrite: Boolean = !HttpMethod(r.getMethod).isSafe

  /**
   * Returns a map of cookie names to lists of their values.  The default
   * value of the map is the empty sequence.
   */
  def multiCookies: MultiParams = {
    val rr = Option(r.getCookies).getOrElse(Array()).toSeq.
      groupBy { _.getName }.
      transform { case(k, v) => v map { _.getValue }}.
      withDefaultValue(Seq.empty)
    MultiParams(rr)
  }


  /**
   * Returns a map of cookie names to values.  If multiple values are present
   * for a given cookie, the value is the first cookie of that name.
   */
  def cookies: Map[String, String] = Params(multiCookies)



  /**
   * The input stream is an InputStream which contains the raw HTTP POST
   * data.  The caller should not close this stream.
   *
   * In contrast to Rack, this stream is not rewindable.
   */
  def inputStream: InputStream = r.getInputStream


  /**
   * The remote address the client is connected from.
   * This takes the load balancing header X-Forwarded-For into account
   * @return the client ip address
   */
  def remoteAddress: String =
    header("X-FORWARDED-FOR").flatMap(_.blankOption) getOrElse r.getRemoteAddr


  /**
   * Returns the preferred <code>Locale</code> that the client will
   * accept content in, based on the Accept-Language header.
   * If the client request doesn't provide an Accept-Language header,
   * this method returns the default locale for the server.
   *
   *  (Quoted from ServletRequest.getLocale)
   *
   * @return the preferred <code>Locale</code> for the client
   */
  def locale: Locale = r.getLocale


  /**
   *
   */
  def locales: java.util.Enumeration[Locale] = r.getLocales

}

