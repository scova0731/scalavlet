package org.scalavlet.support

import org.scalavlet._
import com.typesafe.scalalogging.slf4j.LazyLogging

import javax.servlet.{AsyncEvent, AsyncListener}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.atomic.AtomicBoolean


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

      /**
       * This flag is used for the detection of unexpected completeness by timeout and error
       */
      val isCompleted = new AtomicBoolean(false)

      val context = request.underlying.startAsync()
      context.setTimeout(Configuration.scalavletAsyncTimeout)
      context.addListener(new LoggingAsyncListener(request, isCompleted))

      future.onComplete {
        case Success(v) =>
          if(isCompleted.compareAndSet(false, true)) {
            renderResponseBody(v)
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


  class LoggingAsyncListener(request:Request, isCompleted:AtomicBoolean)
    extends AsyncListener with LazyLogging {

    override def onStartAsync (event: AsyncEvent): Unit = {
      logger.debug(s"Async processing started for ${request.requestURI}")
    }

    override def onError (event: AsyncEvent): Unit = {
      logger.debug(s"Async processing finished with an error in ${request.requestURI}")
      isCompleted.set(true)
      renderResponseBody(Responding.internalServerError(
        body = s"Async processing finished with an error in ${request.requestURI}"))
      event.getAsyncContext.complete()
    }

    override def onTimeout (event: AsyncEvent): Unit = {
      logger.warn(s"Async processing timed out for ${request.requestURI}")
      isCompleted.set(true)
      renderResponseBody(Responding.internalServerError(
        body = s"Async processing timed out for ${request.requestURI}"))
      event.getAsyncContext.complete()
    }

    override def onComplete (event: AsyncEvent): Unit = {
      isCompleted.set(true)
      logger.debug(s"Async processing completed for ${request.requestURI}")
    }
  }
}
