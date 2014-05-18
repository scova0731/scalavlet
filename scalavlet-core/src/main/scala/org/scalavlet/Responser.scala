package org.scalavlet

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.JsonMethods

case class Responser(status: Status, body: Any, headers: Map[String, String])

private object Helpers {
  def status(status: Int, reason: String) = reason match {
    case "" | null => Status(status)
    case _  => new Status(status, reason)
  }
}

import Helpers._


object JsonOption extends Enumeration {
  val DOUBLE, BIGDECIMAL = Value
}

object Responding {
  
  val ok = Ok
  val created = Created
  val accepted = Accepted
  val movedPermanently = MovedPermanently
  val badRequest = BadRequest
  val unauthorized = Unauthorized
  val forbidden = Forbidden
  val notFound = NotFound
  val methodNotAllowed = MethodNotAllowed
  val internalServerError = InternalServerError

  def html(body: Any):Responser =
    Responser(status(200, ""), body, Map("Content-Type" -> "text/html"))

  def plain(body: Any, option:JsonOption.Value = JsonOption.BIGDECIMAL):Responser =
    Responser(status(200, ""), body, Map("Content-Type" -> "text/plain"))


  def json(body: Any,
           option:JsonOption.Value = JsonOption.BIGDECIMAL,
           pretty:Boolean = false):Responser = {
    implicit val formats = DefaultFormats

    def renderJson =
      if (pretty)
        JsonMethods.pretty(render(Extraction.decompose(body)))
      else
        compact(render(Extraction.decompose(body)))


    val renderedBody = option match {
      case JsonOption.BIGDECIMAL =>
        import org.json4s.JsonDSL.WithBigDecimal._
        renderJson
      case JsonOption.DOUBLE =>
        import org.json4s.JsonDSL.WithDouble._
        renderJson

    }

    Responser(status(200, ""), renderedBody, Map("Content-Type" -> "application/json"))
  }


  def redirect(location: String, message: String = "", permanent: Boolean = false): Responser = {
    val msg = if (message == "")
      "Redirecting to <a href=\"%s\">%s</a>.".format(location, location)
    else
      message
    val code = if (permanent) 301 else 302

    Responser(status(code, ""), msg, Map("Location" -> location))
  }
}

trait ShortCutResponser {
  def withNoParams():Responser
}



/** HTTP 200 */
object Ok extends ShortCutResponser {
  def withNoParams() = apply()
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(200, reason), body, headers)

}

/** HTTP 201 */
object Created {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(201, reason), body, headers)
}

/** HTTP 202 */
object Accepted {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(202, reason), body, headers)
}

/** HTTP 203 */
object NonAuthoritativeInformation {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(203, reason), body, headers)
}

/** HTTP 204 */
object NoContent {
  def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(204, reason), Unit, headers)
}

/** HTTP 205 */
object ResetContent {
  def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(205, reason), Unit, headers)
}

/** HTTP 206 */
object PartialContent {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(206, reason), body, headers)
}

/** HTTP 207 */
object MultiStatus {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(207, reason), body, headers)
}

/** HTTP 208 */
object AlreadyReported {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(208, reason), body, headers)
}

/** HTTP 226 */
object IMUsed {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(226, reason), body, headers)
}

/** HTTP 300 */
object MultipleChoices {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(300, reason), body, headers)
}

/** HTTP 301 */
object MovedPermanently {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(301, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 302 */
object Found {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(302, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 303 */
object SeeOther {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(303, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 304 */
object NotModified {
  def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(304, reason), Unit, headers)
}

/** HTTP 305 */
object UseProxy {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(305, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 307 */
object TemporaryRedirect {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(307, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 308 */
object PermanentRedirect {
  def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(308, reason), Unit, Map("Location" -> location) ++ headers)
}

/** HTTP 400 */
object BadRequest {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(400, reason), body, headers)
}

/** HTTP 401 */
object Unauthorized {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(401, reason), body, headers)
}

/** HTTP 402 */
object PaymentRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(402, reason), body, headers)
}

/** HTTP 403 */
object Forbidden {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(403, reason), body, headers)
}

/** HTTP 404 */
object NotFound {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(404, reason), body, headers)
}

/** HTTP 405 */
object MethodNotAllowed {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(405, reason), body, headers)
}

/** HTTP 406 */
object NotAcceptable {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(406, reason), body, headers)
}

/** HTTP 407 */
object ProxyAuthenticationRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(407, reason), body, headers)
}

/** HTTP 408 */
object RequestTimeout {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(408, reason), body, headers)
}

/** HTTP 409 */
object Conflict {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(409, reason), body, headers)
}

/** HTTP 410 */
object Gone {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(410, reason), body, headers)
}

/** HTTP 411 */
object LengthRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(411, reason), body, headers)
}

/** HTTP 412 */
object PreconditionFailed {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(412, reason), body, headers)
}

/** HTTP 413 */
object RequestEntityTooLarge {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(413, reason), body, headers)
}

/** HTTP 414 */
object RequestURITooLong {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(414, reason), body, headers)
}

/** HTTP 415 */
object UnsupportedMediaType {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(415, reason), body, headers)
}

/** HTTP 416 */
object RequestedRangeNotSatisfiable {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(416, reason), body, headers)
}

/** HTTP 417 */
object ExpectationFailed {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(417, reason), body, headers)
}

/** HTTP 422 */
object UnprocessableEntity {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(422, reason), body, headers)
}

/** HTTP 423 */
object Locked {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(423, reason), body, headers)
}

/** HTTP 424 */
object FailedDependency {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(424, reason), body, headers)
}

/** HTTP 426 */
object UpgradeRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(426, reason), body, headers)
}

/** HTTP 428 */
object PreconditionRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(428, reason), body, headers)
}

/** HTTP 429 */
object TooManyRequests {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(429, reason), body, headers)
}

/** HTTP 431 */
object RequestHeaderFieldsTooLarge {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(431, reason), body, headers)
}

/** HTTP 500 */
object InternalServerError {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(500, reason), body, headers)
}

/** HTTP 501 */
object NotImplemented extends ShortCutResponser {
  def withNoParams() = apply()
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(501, reason), body, headers)
}

/** HTTP 502 */
object BadGateway {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(502, reason), body, headers)
}

/** HTTP 503 */
object ServiceUnavailable {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(503, reason), body, headers)
}

/** HTTP 504 */
object GatewayTimeout {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(504, reason), body, headers)
}

/** HTTP 505 */
object HTTPVersionNotSupported {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(505, reason), body, headers)
}

/** HTTP 506 */
object VariantAlsoNegotiates {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(506, reason), body, headers)
}

/** HTTP 507 */
object InsufficientStorage {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(507, reason), body, headers)
}

/** HTTP 508 */
object LoopDetected {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(508, reason), body, headers)
}

/** HTTP 510 */
object NotExtended {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(510, reason), body, headers)
}

/** HTTP 511 */
object NetworkAuthenticationRequired {
  def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
    Responser(status(511, reason), body, headers)
}
