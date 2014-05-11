package org.scalavlet.richer


import javax.servlet.http.HttpSession

// This is added based on the advise of compiler warning
import scala.language.implicitConversions

trait ImplicitRichers {

  implicit def wrapSession(session: HttpSession) =
    new Session(session)


  implicit def wrapString(s: String) = 
    new RichString(s)

}
