package org.scalavlet.route

import rl.UrlCodingUtils


/**
 *
 * Derived from Scalatra
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
