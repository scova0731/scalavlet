package org.scalavlet

sealed trait HttpScheme

case object Http extends HttpScheme

case object Https extends HttpScheme

