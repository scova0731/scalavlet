package org.scalavlet.test

import java.{util => ju}
import javax.servlet.{Filter, DispatcherType}
import javax.servlet.http.HttpServlet

trait Container {
  def contextPath: String = "/"

  def resourceBasePath: String = "src/main/webapp"

  def mount(klass: Class[_], path: String) = klass match {
    case servlet if classOf[HttpServlet].isAssignableFrom(servlet) =>
      addServlet(servlet.asInstanceOf[Class[_ <: HttpServlet]], path)
    case filter if classOf[Filter].isAssignableFrom(filter) =>
      addFilter(filter.asInstanceOf[Class[_ <: Filter]], path)
    case _ =>
      throw new IllegalArgumentException(klass + " is not assignable to either HttpServlet or Filter")
  }


  def mount(servlet: HttpServlet, path: String): Unit =
    addServlet(servlet, path, servlet.getClass.getName)


  def mount(servlet: HttpServlet, path: String, name: String): Unit =
    addServlet(servlet, path, name)


  def mount(app: Filter, path: String,
            dispatches: ju.EnumSet[DispatcherType] = DefaultDispatcherTypes): Unit =
    addFilter(app, path, dispatches)


  protected def addServlet(servlet: HttpServlet, path: String, name: String): Unit


  protected def addServlet(servlet: Class[_ <: HttpServlet], path: String): Unit


  protected def addFilter(filter: Filter, path: String,
                dispatches: ju.EnumSet[DispatcherType] = DefaultDispatcherTypes): Unit


  protected def addFilter(filter: Class[_ <: Filter], path: String): Unit


  protected def ensureSessionIsSerializable(): Unit

  protected def start(): Unit

  protected def stop(): Unit

  protected val DefaultDispatcherTypes: ju.EnumSet[DispatcherType] =
    ju.EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC)

}
