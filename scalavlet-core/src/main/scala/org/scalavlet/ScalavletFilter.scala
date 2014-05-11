package org.scalavlet

import org.scalavlet.route.{Route, RouteTransformer, UriDecoder}
import org.scalavlet.richer.ImplicitRichers

import javax.servlet._

/**
 * An implementation of the Scalavlet DSL in a filter. You may prefer a filter
 * to a ScalatraServlet if:
 *
 * $ - you are sharing a URL space with another servlet or filter and want to
 *     delegate unmatched requests.  This is very useful when migrating
 *     legacy applications one page or resource at a time.
 *
 *
 * Unlike a ScalatraServlet, does not send 404 or 405 errors on non-matching
 * routes.  Instead, it delegates to the filter chain.
 *
 * If in doubt, extend ScalatraServlet instead.
 *
 * @see ScalatraServlet
 */
trait ScalavletFilter extends Filter with ScalavletBase
  with ImplicitRichers
{
  override type ConfigT = FilterConfig

  @deprecated("not have to use directly", "0.1")
  override protected def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain):Unit = {

    handle(request.asInstanceOf[SvRequest], response.asInstanceOf[SvResponse])
    chain.doFilter(request, response)
  }


  def filter(transformers: RouteTransformer*)(action: Request => Any): Route =
    addRoute(SvFilter, transformers, action)


  // val FilterPathKey ="org.scalatra.ScalatraFilter.requestPath"
  // What goes in servletPath and what goes in pathInfo depends on how the underlying servlet is mapped.
  // Unlike the Scalatra servlet, we'll use both here by default.  Don't like it?  Override it.
  override def requestPath(request:Request):String = {
    def getRequestPath = request.requestURI match {
      case requestURI: String =>
        var uri = requestURI
        if (request.contextPath.length > 0) uri = uri.substring(request.contextPath.length)
        if (uri.length == 0) {
          uri = "/"
        } else {
          val pos = uri.indexOf(';')
          if (pos >= 0) uri = uri.substring(0, pos)
        }
        UriDecoder.firstStep(uri)
      case null => "/"
    }
    getRequestPath
  }


  override protected def routeBasePath(request: Request):String = {
    context.path
  }


  /**
   * Do nothing in case of not found
   */
  override protected def notFound(request: Request, response:Response): Action =
    () => Unit



  /**
   * Called by the web container to indicate to a filter that it is
   * being placed into service.
   *
   * <p>The servlet container calls the init
   * method exactly once after instantiating the filter. The init
   * method must complete successfully before the filter is asked to do any
   * filtering work.
   *
   * <p>The web container cannot place the filter into service if the init
   * method either
   * <ol>
   * <li>Throws a ServletException
   * <li>Does not return within a time period defined by the web container
   * </ol>
   *
   * (Quoted from javax.servlet.Filter)
   */
  override def init(config: FilterConfig) {
    initialize(config)
  }


  /**
   * Called by the web container to indicate to a filter that it is being
   * taken out of service.
   *
   * <p>This method is only called once all threads within the filter's
   * doFilter method have exited or after a timeout period has passed.
   * After the web container calls this method, it will not call the
   * doFilter method again on this instance of the filter.
   *
   * <p>This method gives the filter an opportunity to clean up any
   * resources that are being held (for example, memory, file handles,
   * threads) and make sure that any persistent state is synchronized
   * with the filter's current state in memory.
   *
   * (Quoted from javax.servlet.Filter)
   */
  override def destroy() {
    shutdown()
  }
}

