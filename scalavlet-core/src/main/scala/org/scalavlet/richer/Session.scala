package org.scalavlet.richer

import javax.servlet.http.HttpSession
import org.scalavlet.utils.Cacheable

//import org.scalavlet.utils.AttributesMap

/**
 * Extension methods to the standard HttpSession.
 */
class Session(session: HttpSession) extends Cacheable {
  def id = session.getId

  protected def attributes = session
}
