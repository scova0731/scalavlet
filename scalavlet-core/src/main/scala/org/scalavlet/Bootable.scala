package org.scalavlet

import javax.servlet.ServletContext

trait Bootable {

  final def init(context: ServletContext) {
    onStart(new Context(context))
  }

  final def destroy(context: ServletContext) {
    onStop(new Context(context))
  }

  def onStart(context: Context):Unit

  def onStop(context: Context):Unit
}
