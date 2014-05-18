package org.scalavlet.support

import org.scalavlet._
import com.typesafe.scalalogging.slf4j.LazyLogging

import javax.servlet.{AsyncEvent, AsyncListener}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.util.Failure


trait AsyncResponder {

  implicit protected def executor: ExecutionContext

  def request:Request
  def response:Response
  private[scalavlet] def renderResponseBody(actionResult: Any):Unit


  /**
   * Future should be detected first
   */
  protected def asyncRenderPipeline(): PartialFunction[Any, Any] = {
    case future:Future[Any] =>
      val context = request.underlying.startAsync()
      context.setTimeout(Configuration.scalavletAsyncTimeout)
      context.addListener(new LoggingAsyncListener(request, response))

      future.onComplete {
        case Success(v) =>
          try {
            renderResponseBody(v)
          } finally {
            context.complete()
          }
        case Failure(e) =>
          throw new ScalavletAsyncException(e)
      }
      Unit

    case Responser(status, future: Future[Any], headers) =>
      response.setStatus(status)
      headers foreach {
        case (name, value) => response.addHeader(name, value)
      }
      future
  }
}


class LoggingAsyncListener(request:Request, response:Response)
  extends AsyncListener with LazyLogging {

  override def onStartAsync (event: AsyncEvent): Unit = {
    logger.debug(s"Async processing started for ${request.servletPath}")
  }

  override def onError (event: AsyncEvent): Unit = {
    logger.debug(s"Async processing finished with an error in ${request.servletPath}")
  }

  //TODO complete timeout handling for async process
  override def onTimeout (event: AsyncEvent): Unit = {
    event.getAsyncContext.complete()
    logger.warn(s"Async processing timed out for ${request.servletPath}")
  }

  override def onComplete (event: AsyncEvent): Unit = {
    logger.debug(s"Async processing completed for ${request.servletPath}")
  }
}
