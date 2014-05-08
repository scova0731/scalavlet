package org.scalavlet

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
  
  val ok = S200_Ok
  val created = S201_Created
  val accepted = S202_Accepted
  val movedPermanently = S301_MovedPermanently
  val badRequest = S400_BadRequest
  val unauthorized = S401_Unauthorized
  val forbidden = S403_Forbidden
  val notFound = S404_NotFound
  val methodNotAllowed = S405_MethodNotAllowed
  val internalServerError = S500_InternalServerError

  def html(body: Any):Responser =
    Responser(status(200, ""), body, Map("Content-Type" -> "text/html"))

  def plain(body: Any, option:JsonOption.Value = JsonOption.BIGDECIMAL):Responser =
    Responser(status(200, ""), body, Map("Content-Type" -> "text/plain"))


  def json(body: Any, option:JsonOption.Value = JsonOption.BIGDECIMAL):Responser = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.JsonDSL._
    implicit val formats = DefaultFormats

    val renderedBody = option match {
      case JsonOption.BIGDECIMAL =>
        import org.json4s.JsonDSL.WithBigDecimal._
        //r:Response => mapper.writeValue(r.writer, body)  //
        compact(render(Extraction.decompose(body)))
      case JsonOption.DOUBLE =>
        import org.json4s.JsonDSL.WithDouble._
        //r:Response => mapper.writeValue(r.writer, body) //compact(render(body))
        compact(render(Extraction.decompose(body)))

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

  object S200_Ok {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(200, reason), body, headers)
  }

  object S201_Created {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(201, reason), body, headers)
  }

  object S202_Accepted {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(202, reason), body, headers)
  }

  object S203_NonAuthoritativeInformation {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(203, reason), body, headers)
  }

  object S204_NoContent {
    def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(204, reason), Unit, headers)
  }

  object S205_ResetContent {
    def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(205, reason), Unit, headers)
  }

  object S206_PartialContent {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(206, reason), body, headers)
  }

  object S207_MultiStatus {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(207, reason), body, headers)
  }

  object S208_AlreadyReported {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(208, reason), body, headers)
  }

  object S226_IMUsed {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(226, reason), body, headers)
  }

  object S300_MultipleChoices {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(300, reason), body, headers)
  }

  object S301_MovedPermanently {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(301, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S302_Found {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(302, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S303_SeeOther {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(303, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S304_NotModified {
    def apply(headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(304, reason), Unit, headers)
  }

  object S305_UseProxy {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(305, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S307_TemporaryRedirect {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(307, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S308_PermanentRedirect {
    def apply(location: String, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(308, reason), Unit, Map("Location" -> location) ++ headers)
  }

  object S400_BadRequest {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(400, reason), body, headers)
  }

  object S401_Unauthorized {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(401, reason), body, headers)
  }

  object S402_PaymentRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(402, reason), body, headers)
  }

  object S403_Forbidden {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(403, reason), body, headers)
  }

  object S404_NotFound {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(404, reason), body, headers)
  }

  object S405_MethodNotAllowed {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(405, reason), body, headers)
  }

  object S406_NotAcceptable {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(406, reason), body, headers)
  }

  object S407_ProxyAuthenticationRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(407, reason), body, headers)
  }

  object S408_RequestTimeout {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(408, reason), body, headers)
  }

  object S409_Conflict {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(409, reason), body, headers)
  }

  object S410_Gone {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(410, reason), body, headers)
  }

  object S411_LengthRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(411, reason), body, headers)
  }

  object S412_PreconditionFailed {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(412, reason), body, headers)
  }

  object S413_RequestEntityTooLarge {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(413, reason), body, headers)
  }

  object S414_RequestURITooLong {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(414, reason), body, headers)
  }

  object S415_UnsupportedMediaType {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(415, reason), body, headers)
  }

  object S416_RequestedRangeNotSatisfiable {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(416, reason), body, headers)
  }

  object S417_ExpectationFailed {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(417, reason), body, headers)
  }

  object S422_UnprocessableEntity {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(422, reason), body, headers)
  }

  object S423_Locked {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(423, reason), body, headers)
  }

  object S424_FailedDependency {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(424, reason), body, headers)
  }

  object S426_UpgradeRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(426, reason), body, headers)
  }

  object S428_PreconditionRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(428, reason), body, headers)
  }

  object S429_TooManyRequests {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(429, reason), body, headers)
  }

  object S431_RequestHeaderFieldsTooLarge {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(431, reason), body, headers)
  }

  object S500_InternalServerError {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(500, reason), body, headers)
  }

  object S501_NotImplemented {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(501, reason), body, headers)
  }

  object S502_BadGateway {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(502, reason), body, headers)
  }

  object S503_ServiceUnavailable {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(503, reason), body, headers)
  }

  object S504_GatewayTimeout {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(504, reason), body, headers)
  }

  object S505_HTTPVersionNotSupported {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(505, reason), body, headers)
  }

  object S506_VariantAlsoNegotiates {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(506, reason), body, headers)
  }

  object S507_InsufficientStorage {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(507, reason), body, headers)
  }

  object S508_LoopDetected {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(508, reason), body, headers)
  }

  object S510_NotExtended {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(510, reason), body, headers)
  }

  object S511_NetworkAuthenticationRequired {
    def apply(body: Any = Unit, headers: Map[String, String] = Map.empty, reason: String = "") =
      Responser(status(511, reason), body, headers)
  }
}



