package org.scalavlet.test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, BeforeAndAfterAll, Suite}

/**
 * Provides Scalatra test support to ScalaTest suites.  The servlet tester
 * is started before the first test in the suite and stopped after the last.
 */
@RunWith(classOf[JUnitRunner])
trait ScalatraTestHelper
  extends Suite
  with EmbeddedJettyContainer
  with HttpComponentsClient
  with ImplicitConversions
  with BeforeAndAfterAll
  with Matchers {

  override protected def beforeAll(): Unit = start()

  override protected def afterAll(): Unit = stop()
}

