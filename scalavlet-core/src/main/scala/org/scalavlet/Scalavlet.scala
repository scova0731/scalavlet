package org.scalavlet

import org.scalavlet.route.{Route, RouteTransformer, UriDecoder}
import org.scalavlet.richer.ImplicitRichers

import javax.servlet.http.HttpServlet
import javax.servlet.ServletConfig

import scala.util.control.Exception.catching


abstract class Scalavlet
  extends HttpServlet 
  with ScalavletBase
  with ImplicitRichers {

  /**
   * Servlet specific config type
   */
  override type ConfigT = ServletConfig


  /**
   * service() method is the start point of the request. The process is
   * delegated to handle() method. Usually, this doesn't have to be called
   * directly.
   */
  override def service(request: SvRequest, response: SvResponse):Unit = {
    //super.handle does NOT need to be called
    handle(request, response)
  }


  /**
   * The Scalavlet DSL core methods take a list of [[org.scalavlet.route.RouteMatcher]]
   * and a block as the action body.  The return value of the block is
   * rendered through the pipeline and sent to the client as the response body.
   *
   * See [[org.scalavlet.ScalavletBase#renderResponseBody]] for the detailed
   * behaviour and how to handle your response body more explicitly, and see
   * how different return types are handled.
   *
   * The block is executed in the context of a CoreDsl instance, so all the
   * methods defined in this trait are also available inside the block.
   *
   * {{{
   *   post("/echo") {
   *     "hello {params('name)}!"
   *   }
   * }}}
   *
   * Scalavlet provides implicit transformation from boolean blocks,
   * strings and regular expressions to [[org.scalavlet.RouteMatcher]], so
   * you can write code naturally.
   * {{{
   *   get("/", request.getRemoteHost == "127.0.0.1") { "Hello localhost!" }
   * }}}
   *
   * Most of this routing part is from Scalatra
   *
   */
  def get(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Get, transformers, action)

  def post(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Post, transformers, action)

  def put(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Put, transformers, action)

  def delete(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Delete, transformers, action)

  def options(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Options, transformers, action)

  def head(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Head, transformers, action)

  def patch(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(Patch, transformers, action)


//  def onCompleted(fn: Try[Any] => Unit): Unit =
//    callbacks += fn

  override protected def routeBasePath(request: Request) = {
    context.path + request.servletPath
  }


  /**
   * Defines the request path to be matched by routers.  The default
   * definition is optimized for `path mapped` servlets (i.e., servlet
   * mapping ends in `&#47;*`).  The route should match everything matched by
   * the `&#47;*`.  In the event that the request URI equals the servlet path
   * with no trailing slash (e.g., mapping = `/admin&#47;*`, request URI =
   * '/admin'), a '/' is returned.
   *
   * All other servlet mappings likely want to return request.getServletPath.
   * Custom implementations are allowed for unusual cases.
   */
  //TODO know this
  override def requestPath(request:Request):String = {
    def startIndex (r: Request) =
      r.contextPath.blankOption.map(_.length).getOrElse(0) + r.servletPath.blankOption.map(_.length).getOrElse(0)
    def getRequestPath (r: Request) = {
      val u = catching(classOf[NullPointerException]) opt {
        r.requestURI
      } getOrElse "/"
      requestPath(u, startIndex(r))
    }
    getRequestPath(request)
  }


  def requestPath(uri: String, idx: Int): String = {
    val u1 = UriDecoder.firstStep(uri)
    val u2 = u1.blankOption map { _.substring(idx) } flatMap(_.blankOption) getOrElse "/"
    val pos = u2.indexOf(';')
    if (pos > -1) u2.substring(0, pos) else u2
  }


  /**
   * Invoked when no route matches. By default, calls `serveStaticResource()`,
   * and if that fails, calls `resourceNotFound()`.
   */
  override protected def notFound(request: Request, response:Response): Action =
    () => serveStaticResource(request.underlying, response.underlying).
      getOrElse(resourceNotFound(response))


  /**
   * Attempts to find a static resource matching the request path.  Override
   * to return None to stop this.
   */
  protected def serveStaticResource(request:SvRequest, response:SvResponse): Option[Any] =
    context.resource(request) map { _ =>
      context.namedDispatcher("default").
        forward(
          request,
          response match {
            case res:WrappedGZipResponse =>
              res.skip = true
              res.underlying
            case res => res
          }
        )
    }


  /**
   * Called by default doNotFound if no routes matched and no static resource
   * could be found.
   */
  protected def resourceNotFound(response:Response): Any = {
    Unit
    //TODO visit dev-mode later
//    if (isDevelopmentMode) {
//      val error = "Requesting \"%s %s\" on servlet \"%s\" but only have: %s"
//      response.getWriter println error.format(
//        request.getMethod,
//        Option(request.getPathInfo) getOrElse "/",
//        request.getServletPath,
//        routes.entryPoints.mkString("<ul><li>", "</li><li>", "</li></ul>"))
//    }
  }


  /**
   *
   */
  override def init(config: ServletConfig) {
    super.init(config)
    initialize(config)
  }


  /**
   *
   */
  override def destroy() {
    shutdown()
    super.destroy()
  }
}
