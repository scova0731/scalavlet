package org.scalavlet.richer


import javax.servlet.ServletContext
import javax.servlet.http.{HttpSession, HttpServletResponse, HttpServletRequest}

// This is added based on the advise of compiler warning
import scala.language.implicitConversions

trait ImplicitRichers {

//  implicit def wrapServletContext(context: ServletContext) =
//    new Context(context)

  
  implicit def wrapSession(session: HttpSession) =
    new Session(session)

  
//  implicit def wrapRequest(request: HttpServletRequest) =
//    new Request(request)

  
//  implicit def wrapResponse(response: HttpServletResponse) =
//    new Response(response)


  implicit def wrapString(s: String) = 
    new RichString(s)

}
