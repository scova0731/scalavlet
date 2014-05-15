package org.scalavlet

import javax.servlet.http.HttpSession
import org.scalavlet.utils.Cacheable


/**
 * Extension methods to the standard HttpSession.
 */
class Session(s: HttpSession) extends Cacheable {

  def underlying = s


  def id = s.getId

}
