package org.scalavlet.route

import rl.UrlCodingUtils


/**
 * copied from org.scalatra.ScalatraBase
 */
object UriDecoder {
  def firstStep(uri: String) =
    UrlCodingUtils.urlDecode(
      UrlCodingUtils.ensureUrlEncoding(uri),
      toSkip = "/?#"
    )

  def secondStep(uri: String) =
    uri.
      replaceAll("%23", "#").
      replaceAll("%2F", "/").
      replaceAll("%3F", "?")
}
