package org.scalavlet.examples.simple

import org.scalavlet.ScalavletBootable
import org.scalavlet.Context


class Bootstrap extends ScalavletBootable {


  override def onStart (context: Context): Unit = {
    context.mount(new PageServlet, "/")
  }

  override def onStop (context: Context): Unit = {
    context.log("Sample project is shutting down !")
  }
}
