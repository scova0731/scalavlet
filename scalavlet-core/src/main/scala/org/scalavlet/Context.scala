package org.scalavlet

import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.ServletContext
import javax.servlet.http.HttpServlet

import scala.reflect.ClassTag
import java.net.URL
import java.{util => ju}
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * Defines a set of methods that a servlet uses to communicate with its
 * servlet container, for example, to get the MIME type of a file, dispatch
 * requests, or write to a log file.
 *
 * <p>There is one context per "web application" per Java Virtual Machine.  (A
 * "web application" is a collection of servlets and content installed under a
 * specific subset of the server's URL namespace such as <code>/catalog</code>
 * and possibly installed via a <code>.war</code> file.)
 *
 * <p>In the case of a web
 * application marked "distributed" in its deployment descriptor, there will
 * be one context instance for each virtual machine.  In this situation, the
 * context cannot be used as a location to share global information (because
 * the information won't be truly global).  Use an external resource like
 * a database instead.
 *
 * <p>The <code>ServletContext</code> object is contained within
 * the {@link ServletConfig} object, which the Web server provides the
 * servlet when the servlet is initialized.
 *
 * (Quoted from javax.servlet.ServletContext interface)
 *
 * @see 	javax.servlet.ServletContext
 * @see   javax.servlet.ServletRegistration
 */
class Context(c: ServletContext) extends LazyLogging {

  def underlying: ServletContext = c


  def path: String = c.getContextPath


  def namedDispatcher(name: String) = c.getNamedDispatcher(name: String)


  def log(msg: String, ex: Throwable) = c.log(msg, ex)


  def log(msg: String) = c.log(msg)


  /**
   * Optionally returns a URL to the resource mapped to the given path.  This
   * is a wrapper around `getResource`.
   *
   * @param path the path to the resource
   * @return the resource located at the path, or None if there is no resource
   * at that path.
   * @throws MalformedURLException if the pathname is not given in
   * the correct form
   */
  def resource(path: String): Option[URL] =
    Option(c.getResource(path))


  /**
   * Optionally returns the resource mapped to the request's path.
   *
   * @param req the request
   * @return the resource located at the result of concatenating the request's
   * servlet path and its path info, or None if there is no resource at that path.
   */
  def resource(req: Request): Option[URL] =
    resource(req.servletPath + Option(req.pathInfo).getOrElse(""))


  def resource(req: SvRequest): Option[URL] =
    resource(req.getServletPath + Option(req.getPathInfo).getOrElse(""))


  /**
   * Mounts a base to the servlet context.  Must be an HttpServlet or a
   * Filter.
   *
   * @param base the base to mount
   *
   * @param urlPattern the URL pattern to mount.  Will be appended with `\/\*` if
   * not already, as path-mapping is the most natural fit for Scalatra.
   * If you don't want path mapping, use the native Servlet API.
   */
  def mount(base: ScalavletBase, urlPattern: String, loadOnStartup: Int = 1):Context = {
    base match {
      case servlet: HttpServlet =>
        mountServlet(servlet, completeMapping(urlPattern), loadOnStartup)
      case filter: Filter =>
        mountFilter(filter, completeMapping(urlPattern))
      case _ =>
        logger.error(base.getClass.toString)
    }
    this
  }


  /**
   * 
   * @param urlPattern
   * @param loadOnStartup
   * @return
   */
  def mount[T](urlPattern: String, loadOnStartup: Int = 1)(implicit ct:ClassTag[T]):Context = {
    if (classOf[HttpServlet].isAssignableFrom(ct.runtimeClass))
      mountServlet(ct.runtimeClass.asInstanceOf[Class[HttpServlet]],
        completeMapping(urlPattern), loadOnStartup)
    else if (classOf[Filter].isAssignableFrom(ct.runtimeClass))
      mountFilter(ct.runtimeClass.asInstanceOf[Class[Filter]],
        completeMapping(urlPattern))
    else
      logger.error(ct.runtimeClass.toString)
    this
  }


  private def mountServlet(servlet: HttpServlet, urlPattern: String, loadOnStartup: Int):Unit = {
    val reg = Option(c.getServletRegistration(servlet.getClass.getName)) getOrElse {
      val r = c.addServlet(servlet.getClass.getName, servlet)
      r.setAsyncSupported(true)
      r.setLoadOnStartup(loadOnStartup)
      r
    }
    reg.addMapping(urlPattern)
  }

  
  private def mountServlet(servletClass: Class[HttpServlet], urlPattern: String, loadOnStartup: Int):Unit = {
    val name = servletClass.getName
    val reg = Option(c.getServletRegistration(name)) getOrElse {
      val r = c.addServlet(name, servletClass)
      r.setAsyncSupported(true)
      r.setLoadOnStartup(loadOnStartup)
      r
    }
    reg.addMapping(urlPattern)
  }


  private def mountFilter(filter: Filter, urlPattern: String):Unit =
    c.addFilter(filter.getClass.getName, filter).
      addMappingForUrlPatterns(dispatchers, true, urlPattern)


  private def mountFilter(filterClass: Class[Filter], urlPattern: String):Unit =
    c.addFilter(filterClass.getName, filterClass).
      addMappingForUrlPatterns(dispatchers, true, urlPattern)


  private def completeMapping(urlPattern: String):String = urlPattern match {
    case s if s.endsWith("/*") => s
    case s if s.endsWith("/") => s + "*"
    case s => s + "/*"
  }


  private val dispatchers = ju.EnumSet.allOf(classOf[DispatcherType])
}
