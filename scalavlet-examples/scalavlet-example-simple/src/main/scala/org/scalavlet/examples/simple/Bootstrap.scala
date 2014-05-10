package org.scalavlet.examples.simple

import org.scalavlet.Bootable
import org.scalavlet.Context


class Bootstrap extends Bootable {


  override def onStart (context: Context): Unit = {
    context.mount(PageServlet, "/")
  }

  override def onStop (context: Context): Unit = {
    context.log("Sample project is shutting down !")
  }
}
