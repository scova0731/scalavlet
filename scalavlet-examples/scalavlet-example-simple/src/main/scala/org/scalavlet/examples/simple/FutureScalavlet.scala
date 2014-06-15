package org.scalavlet.examples.simple

import org.scalavlet.Scalavlet
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.concurrent.{Await, Future, future}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global


object FutureScalavlet extends Scalavlet with LazyLogging {


  /**
   * Process a slow async operation using Servlet async feature.
   */
  get("/slow-async"){ request =>
    future {
      Thread.sleep(1000)
      "Response After 1000 ms !"
    }
  }


  /**
   * Process a slow async operation using Scala async future.
   */
  get("/slow-await"){ request =>
    Await.result(
      Future {
        Thread.sleep(1000)
        "Response After 1000 ms !"
      },
      1 second)
  }
}
