package org.scalavlet

import org.scalatest.{Matchers, FlatSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SandboxTest extends FlatSpec with Matchers {

  "Symbol in Map" should "OK" in {

    val key1 = "key1"
    val key2 = "key1"

    val map1 = Map(key1 -> "value1")

    map1(key1) should be("value1")
    map1(key2) should be("value1")
    map1("key1") should be("value1")

  }


}
