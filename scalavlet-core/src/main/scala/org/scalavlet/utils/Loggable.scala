package org.scalavlet.utils

import scala.reflect.ClassTag
import org.slf4j.{Logger, LoggerFactory}

trait Loggable {

  def loggerOf[A](implicit ct:ClassTag[A]):Logger =
    LoggerFactory.getLogger(ct.runtimeClass)

}
