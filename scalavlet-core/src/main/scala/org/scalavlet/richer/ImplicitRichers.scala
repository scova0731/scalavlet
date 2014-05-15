package org.scalavlet.richer


// This is added based on the advise of compiler warning
import scala.language.implicitConversions

trait ImplicitRichers {

  implicit def wrapString(s: String) = 
    new RichString(s)

}
