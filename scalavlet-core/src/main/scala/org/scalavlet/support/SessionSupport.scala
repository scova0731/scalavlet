package org.scalavlet.support

import org.scalavlet.{Session, Request, SvRequest}

import javax.servlet.http.HttpSession


/**
 * This trait provides session support for stateful applications.
 */
trait SessionSupport {

  /**
   * The current session.  Creates a session if none exists.
   */
  implicit def session(implicit request: Request): Session =
    new Session(request.session)


  /**
   * Get a value from the current session
   */
  def session(key: String)(implicit request: Request): Any =
    session(request).cache(key)


  /**
   * The current session.  If none exists, None is returned.
   */
  def sessionOption(implicit request: SvRequest): Option[HttpSession] =
    Option(request.getSession(false))
}
