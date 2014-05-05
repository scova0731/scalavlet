package org.scalavlet.test

import javax.servlet.{DispatcherType, Filter}
import javax.servlet.http.HttpServlet
import org.eclipse.jetty.servlet._

import java.{util => ju}


trait JettyContainer extends Container {

  // Add a default servlet.  If there is no underlying servlet, then
  // filters just return 404.
  if (!skipDefaultServlet)
    servletContextHandler.addServlet(new ServletHolder("default", classOf[DefaultServlet]), "/")


  def servletContextHandler: ServletContextHandler


  def skipDefaultServlet: Boolean = false


  override def addServlet(servlet: HttpServlet, path: String, name: String): Unit ={
    val holder = new ServletHolder(name, servlet)

//    servlet match {
//      case s: HasMultipartConfig => {
//        holder.getRegistration.setMultipartConfig(
//          s.multipartConfig.toMultipartConfigElement)
//      }
//      case s: ScalatraAsyncSupport =>
//        holder.getRegistration.setAsyncSupported(true)
//      case _ =>
//    }

    servletContextHandler.addServlet(holder, if (path.endsWith("/*")) path else path + "/*")

  }


  override protected def addServlet(servlet: Class[_ <: HttpServlet], path: String): Unit =
    servletContextHandler.addServlet(servlet, path)


  override protected def addFilter(filter: Filter, path: String,
                dispatches: ju.EnumSet[DispatcherType] = DefaultDispatcherTypes): Unit = {
    val holder = new FilterHolder(filter)
    servletContextHandler.addFilter(holder, path, dispatches)
  }


  override protected def addFilter(filter: Class[_ <: Filter], path: String): Unit =
    addFilter(filter, path, DefaultDispatcherTypes)


  protected def addFilter(filter: Class[_ <: Filter], path: String,
                dispatches: ju.EnumSet[DispatcherType]): Unit =
    servletContextHandler.addFilter(filter, path, dispatches)


  override protected def ensureSessionIsSerializable(): Unit = {
    servletContextHandler.getSessionHandler.addEventListener(SessionSerializingListener)
  }
}
