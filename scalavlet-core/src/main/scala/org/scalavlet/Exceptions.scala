package org.scalavlet

import scala.util.control.NoStackTrace

/**
 * General exception raised inside Scalavlet framework
 */
class ScalavletException(msg:String, cause:Throwable)
  extends RuntimeException(msg, cause) {

  def this() = this(null, null)
  def this(msg:String) = this(msg, null)
  def this(cause:Throwable) = this(cause.getMessage, cause)
}


/**
 * Async processing specific exception usually raised inside Async support
 */
class ScalavletAsyncException(msg:String, cause:Throwable)
  extends ScalavletException(msg, cause) {

  def this() = this(null, null)
  def this(msg:String) = this(msg, null)
  def this(cause:Throwable) = this(cause.getMessage, cause)
}


/**
 * Exception for configuration read
 */
class ScalavletConfigException(path:String, msg:String, cause:Throwable)
  extends ScalavletException(msg, cause) {

  def this(path:String, msg:String) = this(path, msg, null)
}


//TODO complete the exception
class ScalavletPassException
  extends Throwable with NoStackTrace

