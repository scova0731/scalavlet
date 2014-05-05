package org.scalavlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * A `Handler` is the Scalatra abstraction for an object that operates on
 * a request/response pair.
 */
trait Handler {
  /**
   * Handles a request and writes to the response.
   */
  protected def handle(request: HttpServletRequest, res: HttpServletResponse): Unit
}
