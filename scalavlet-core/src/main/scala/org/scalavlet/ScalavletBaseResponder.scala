package org.scalavlet

import org.scalavlet.utils.{StringHelpers, FileCharset}
import org.scalavlet.utils.Commons._
import org.scalavlet.richer.ImplicitRichers
import org.scalavlet.route.{RouteRegistry, UriDecoder, MatchedRoute}

import scala.annotation.tailrec
import java.io.{FileInputStream, File}

import StringHelpers._


class ScalavletBaseResponder(
  request: Request,
  response: Response,
  routes:RouteRegistry,
  doNotFound: (Request, Response) => Action,
  requestPath: Request => String) extends ImplicitRichers {

  /**
   * The error handler function, called if an exception is thrown during
   * before filters or the routes.
   */
  protected var errorHandler: PartialFunction[Throwable, Any] = { case t => throw t }


  def executeRoutes(): Unit = {

    var result: Any = null
    var rendered = true

    def runActions(): Unit = {
      val actionResult = runRoutes()
      // Give the status code handler a chance to override the actionResult
      result = actionResult orElse matchOtherMethods getOrElse doNotFound(request, response)
      rendered = false
      result
    }

    cradleHalt(
      runActions,
      renderUncaughtException
    )

    if (!rendered) renderResponse(result)
  }


  private[scalavlet] def cradleHalt(body: () => Unit, error: Throwable => Unit): Unit =
    try {
      body()
    } catch {
      case e: Throwable => error(e)
    }


  //TODO handle development mode
  private[scalavlet] def renderUncaughtException(e: Throwable):Unit = {
    response.setStatus(Status(500))
    //if (isDevelopmentMode) {
    response.setContentType(Some("text/plain"))
    e.printStackTrace(response.writer)
    //}
  }


  /**
   * Renders the action result to the response.
   * $ - If the content type is still null, call the contentTypeInferrer.
   * $ - Call the render pipeline on the result.
   */
  private[scalavlet] def renderResponse(actionResult: Any):Unit = {
    if (response.contentType.isEmpty)
      response.setContentType(contentTypeInferrer.lift(actionResult))

    renderResponseBody(actionResult)
  }


  /**
   * Renders the action result to the response body via the render pipeline.
   *
   * @see #renderPipeline
   */
  private[scalavlet]  def renderResponseBody(actionResult: Any):Unit = {
    @tailrec def loop(ar: Any): Any = ar match {
      case _: Unit | Unit =>
        //runRenderCallbacks(Success(actionResult))
      case a =>
        loop(renderPipeline().lift(a).orNull)
    }

    try {
      loop(actionResult)
    } catch {
      case e: Throwable =>
        renderUncaughtException(e)
    }
  }


  /**
   * The render pipeline is a partial function of Any => Any.  It is
   * called recursively until it returns ().  () indicates that the
   * response has been rendered.
   */
  private[scalavlet]  def renderPipeline(): PartialFunction[Any, Any] = {
    //If body is Int, it should be distinguished from normal Int
    case Responser(status, body: Int, resultHeaders) =>
      response.setStatus(status)
      resultHeaders foreach {
        case (name, value) => response.addHeader(name, value)
      }
      response.writer.print(body.toString)

    case Responser(Status(404, _), _: Unit | Unit, _) =>
      response.setStatus(404)
      doNotFound(request, response)

    //Set status and headers, then delegate
    case ar: Responser =>
      response.setStatus(ar.status)
      ar.headers.foreach {
        case (name, value) => response.addHeader(name, value)
      }
      ar.body

    case status: Int =>
      if (status == 404) {
        response.setStatus(404)
        doNotFound(request, response)
      } else
        response.setStatus(Status(status))

    case bytes: Array[Byte] =>
      if (response.contentType.isDefined && response.contentType.get.startsWith("text"))
        response.setCharacterEncoding(Some(FileCharset(bytes).name))
      response.outputStream.write(bytes)

    case is: java.io.InputStream =>
      using(is) {
        copy(_, response.outputStream)
      }

    case file: File =>
      if (response.contentType.isDefined && response.contentType.get.startsWith("text"))
        response.setCharacterEncoding(Some(FileCharset(file).name))
      using(new FileInputStream(file)) {
        in => zeroCopy(in, response.outputStream)
      }

    // If an action returns Unit, it assumes responsibility for the response
    case _: Unit | Unit | null =>
      Unit

    case lazyAction: Action =>
      lazyAction()

    case x =>
      response.writer.print(x.toString)
  }







  /**
   * Lazily invokes routes with `invoke`.  The results of the routes
   * are returned as a stream.
   */
  private[scalavlet] def runRoutes(): Option[Any] = {
    (for {
      route <- routes(request.requestMethod).toStream // toStream makes it lazy so we stop after match
      matchedRoute <- route.find(requestPath(request))
      actionResult <- invoke(matchedRoute)
    } yield actionResult).headOption
  }


  // TODO learn Scalatra
  private[scalavlet] def matchOtherMethods: Option[Any] = {
    val allow = routes.matchingMethodsExcept(request.requestMethod, requestPath(request))
    if (allow.isEmpty)
      None
    else
      liftAction(q => doMethodNotAllowed(allow))
  }


  private[scalavlet] def invoke(mRoute:MatchedRoute): Option[Any] = {

    //TODO remove stateful cache later. instead pass them as params
    val decoded = mRoute.multiParams.map {
      case (key, values) =>
        key -> values.map(s => if (s.nonBlank) UriDecoder.secondStep(s) else s)
    }
    request.addParamsCache(decoded)

    liftAction(mRoute.action)
  }


  private[scalavlet] def liftAction(action: Action2): Option[Any] =
    Some(action(request))


  /**
   * Called if no route matches the current request method, but routes
   * match for other methods.  By default, sends an HTTP status of 405
   * and an `Allow` header containing a comma-delimited list of the allowed
   * methods.
   */
  private[scalavlet] val doMethodNotAllowed:(Set[HttpMethod] => Any) = {
    allow =>
      response.setStatus(Status(405))
      response.headers("Allow") = allow.mkString(", ")
  }
}
