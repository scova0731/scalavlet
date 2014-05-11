package org.scalavlet

import org.scalavlet.route.{Route, RouteTransformer, RouteRegistry, ImplicitRouteMatchers}
import org.scalavlet.support.SessionSupport
import org.scalavlet.utils.{Loggable, StringHelpers}
import org.slf4j.LoggerFactory
import javax.servlet.{ServletOutputStream, ServletContext}
import javax.servlet.http.{HttpServletResponseWrapper, HttpServletResponse}

import scala.collection.immutable.DefaultMap
import scala.collection.JavaConverters._
import java.util.zip.GZIPOutputStream
import java.io.PrintWriter
import scala.reflect.ClassTag

//import reflectiveCalls for Config trait
import scala.language.reflectiveCalls

import StringHelpers._


trait ScalavletBase
  extends ImplicitRouteMatchers
  with SessionSupport
  with Handler
  with Loggable {

  protected val logger = loggerOf[ScalavletBase]

  /**
   * The default character encoding for requests and responses.
   */
  //TODO move out
  protected val defaultCharacterEncoding = "UTF-8"


  /**
   * The routes registered in this kernel.
   */
  protected lazy val routes = new RouteRegistry


  /**
   * The effective path against which routes are matched.  The definition
   * varies between servlets and filters.
   */
  def requestPath(request:Request): String


  /**
   * The base path for URL generation
   */
  protected def routeBasePath(request: Request): String



  type ConfigT <: {
    def getServletContext(): ServletContext
    def getInitParameter(name: String): String
    def getInitParameterNames(): java.util.Enumeration[String]
  }


  trait Config {
    def context: Context
    def initParameter(name: String): Option[String]
    def initParametersNames: Iterator[String]
    def initParameters: Map[String, String]
  }


  /**
   * The configuration, typically a ServletConfig or FilterConfig.
   */
  var servletConfig: Config = _



  /**
   * Handles a request and renders a response.
   */
  override protected def handle(request: SvRequest, response: SvResponse) {
    val wRequest = new Request(request, response)
    val wResponse = new Response(response)

    //request(CookieSupport.SweetCookiesKey) = new SweetCookies(request.cookies, response)
    wResponse.setCharacterEncoding(Some(defaultCharacterEncoding))

    withGZipSupport(wRequest, wResponse) { (q, r) =>
      new ScalavletBaseResponder(
        q,
        r,
        routes,
        notFound,
        requestPath).executeRoutes()
    }
  }


  /**
   * Return the configuration singleton
   */
  def config:Configuration = Configuration()


//  /**
//   * EXPERIMENTAL
//   */
//  def configOf[A](path: String)(implicit ct:ClassTag[A]):Option[A] =
//    ct.runtimeClass.getSimpleName match {
//      case "String" => config.getString(path).asInstanceOf[Option[A]]
//      case "Int" => config.getInt(path).asInstanceOf[Option[A]]
//      case "Long" => config.getLong(path).asInstanceOf[Option[A]]
//      case "Double" => config.getDouble(path).asInstanceOf[Option[A]]
//      case _=> None
//    }



  /**
   * Provide Responding object as a response builder.
   */
  def respond:Responding.type = Responding



  /**
   * Builds a full URL from the given relative path. Takes into account the port configuration, https, ...
   *
   * @param path a relative path
   *
   * @return the full URL
   */
  def fullUrl(request:Request,
              path: String,
              params: Iterable[(String, Any)] = Iterable.empty,
              includeContextPath: Boolean = true,
              includeServletPath: Boolean = true,
              withSessionId: Boolean = true) = {

    if (path.startsWith("http")) path
    else {
      val p = url(request, path, params, includeContextPath, includeServletPath, withSessionId)
      if (p.startsWith("http")) p else request.buildBaseUrl + ensureSlash(p)
    }
  }


  /**
   * Returns a context-relative, session-aware URL for a path and specified
   * parameters.
   * Finally, the result is run through `response.encodeURL` for a session
   * ID, if necessary.
   *
   * @param path the base path.  If a path begins with '/', then the context
   *             path will be prepended to the result
   *
   * @param params params, to be appended in the form of a query string
   *
   * @return the path plus the query string, if any.  The path is run through
   *         `response.encodeURL` to add any necessary session tracking parameters.
   */
  def url(request: Request,
          path: String,
          params: Iterable[(String, Any)] = Iterable.empty,
          includeContextPath: Boolean = true,
          includeServletPath: Boolean = true,
          absolutize: Boolean = true,
          withSessionId: Boolean = true): String = {

    val servletPath = request.servletPath.blankOption
    val newPath = path match {
      case x if x.startsWith("/") && includeContextPath && includeServletPath =>
        ensureSlash(routeBasePath(request)) + ensureContextPathsStripped(servletPath, ensureSlash(path), context.path)
      case x if x.startsWith("/") && includeContextPath =>
        ensureSlash(context.path) + ensureContextPathStripped(ensureSlash(path), context.path)
      case x if x.startsWith("/") && includeServletPath => servletPath map {
        ensureSlash(_) + ensureServletPathStripped(servletPath, ensureSlash(path))
      } getOrElse "/"
      case _ if absolutize => ensureContextPathsStripped(servletPath, ensureSlash(path), context.path)
      case _ => path
    }

    val pairs = params map {
      case (key, None) => key.urlEncode + "="
      case (key, Some(value)) => key.urlEncode + "=" + value.toString.urlEncode
      case (key, value) => key.urlEncode + "=" + value.toString.urlEncode
    }
    val queryString = if (pairs.isEmpty) "" else pairs.mkString("?", "&", "")

    if (withSessionId)
      request.underlyingResponse.encodeURL(newPath + queryString)
    else
      newPath + queryString
  }


  /**
   * The servlet context in which this kernel runs.
   */
  def context: Context = servletConfig.context


  /**
   * A hook to initialize the class with some configuration after it has
   * been constructed.
   *
   * Not called init because GenericServlet doesn't override it, and then
   * we get into https://lampsvn.epfl.ch/trac/scala/ticket/2497.
   */
  protected def initialize(config: ConfigT):Unit =  {
    this.servletConfig = new Config {
      def context = new Context(config.getServletContext())
      def initParameter(name: String) = Option(config.getInitParameter(name))
      def initParametersNames = config.getInitParameterNames().asScala
      object initParameters extends DefaultMap[String, String] {
        def get(key: String): Option[String] = Option(config.getInitParameter(key))
        def iterator: Iterator[(String, String)] =
          for (name <- config.getInitParameterNames().asScala)
          yield (name, config.getInitParameter(name))
      }
    }
  }


  /**
   * Called if no route matches the current request for any method.  The
   * default implementation varies between servlet and filter.
   */
  //  protected def doNotFound(request: Request, response: Response): Action
  protected def notFound(request: Request, response:Response): Action


  /**
   * Prepends a new route for the given HTTP method.
   *
   * Can be overriden so that subtraits can use their own logic.
   * Possible examples:
   * $ - restricting protocols
   * $ - namespace routes based on class name
   * $ - raising errors on overlapping entries.
   *
   * This is the method invoked by get(), post() etc.
   *
   * @see org.scalatra.ScalatraKernel#removeRoute
   */
  protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: Request => Any): Route = {
    val route = Route.apply(transformers, action, (q: Request) => routeBasePath(q))
    routes.prependRoute(method, route)
    route
  }


  /**
   * A hook to shutdown the class.  Bridges the gap between servlet's
   * destroy and filter's destroy.
   */
  protected def shutdown(): Unit = {}


  /**
   * from org.scalatra.GZipSupport
   */
  private[scalavlet] def withGZipSupport(request:Request, response:Response)
                                        (callee: (Request, Response) => Unit):Unit = {
    if (request.isGZip) {
      val gzos = new GZIPOutputStream(response.outputStream)
      val gzsos = new GZipServletOutputStream(gzos, response.outputStream)
      val w = new PrintWriter(gzos)
      val gzres = new WrappedGZipResponse(response.underlying, gzsos, w)
      val wrappedResponse = new Response(gzres)

      callee(request, wrappedResponse)

      if (!gzres.skip) {
        response.addHeader("Content-Encoding", "gzip")
        w.flush()
        w.close()
      }

    } else
      callee(request, response)
  }
}


class GZipServletOutputStream(gzos: GZIPOutputStream, orig: ServletOutputStream) extends ServletOutputStream {
  override def write(b: Int): Unit = gzos.write(b)
  //      override def setWriteListener(writeListener: WriteListener): Unit = orig.setWriteListener(writeListener)
  //      override def isReady: Boolean = orig.isReady()
}


//TODO make "skip" immutable
class WrappedGZipResponse(val underlying: HttpServletResponse, gzsos: ServletOutputStream, w: PrintWriter,
                          var skip: Boolean = false) extends HttpServletResponseWrapper(underlying) {
  override def getOutputStream: ServletOutputStream = gzsos
  override def getWriter: PrintWriter = w
  override def setContentLength(i: Int) = {} // ignoring content length as it won't be the same when gzipped
}
