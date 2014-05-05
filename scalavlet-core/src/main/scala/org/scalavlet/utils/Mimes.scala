package org.scalavlet.utils

import eu.medsea.util.EncodingGuesser
import eu.medsea.mimeutil.{ MimeType, MimeUtil2 }
import scala.util.control.Exception._
import java.io.{ InputStream, File }
import java.net.{URI, URL}

import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

object Mimes {

  val DefaultMime = "application/octet-stream"
  /**
   * Sets supported encodings for the mime-util library if they have not been
   * set. Since the supported encodings is stored as a static Set we
   * synchronize access.
   */
  private def registerEncodingsIfNotSet():Unit = {
    synchronized {
      if (EncodingGuesser.getSupportedEncodings.isEmpty) {
        val enc = Set("UTF-8", "ISO-8859-1", "windows-1252", "MacRoman", EncodingGuesser.getDefaultEncoding)
        EncodingGuesser.setSupportedEncodings(enc)
      }
    }
  }
  registerEncodingsIfNotSet()
}


/**
 * A utility to help with mime type detection for a given file path or url
 */
trait Mimes {
  import Mimes._

  //@transient private[this] val internalLogger = Logger(getClass)
  val logger = LoggerFactory.getLogger(getClass)

  protected[this] def mimeUtil = new MimeUtil2()

  quiet {
    mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector")
  }
  quiet {
    mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector")
  }

  private def quiet(fn: ⇒ Unit) =
    allCatch.withApply(logger.warn("An error occurred while registering a mime type detector.", _))(fn)



  def bytesMime(content: Array[Byte], fallback: String = DefaultMime) = {
    detectMime(fallback) {
      MimeUtil2.getMostSpecificMimeType(
        mimeUtil.getMimeTypes(content, new MimeType(fallback))).toString
    }
  }
  def apply(bytes: Array[Byte]): String = bytesMime(bytes)


  def fileMime(file: File, fallback: String = DefaultMime) = {
    detectMime(fallback) {
      MimeUtil2.getMostSpecificMimeType(
        mimeUtil.getMimeTypes(file, new MimeType(fallback))).toString
    }
  }
  def apply(file: File): String = fileMime(file)


  def inputStreamMime(input: InputStream, fallback: String = DefaultMime) = {
    detectMime(fallback) {
      MimeUtil2.getMostSpecificMimeType(
        mimeUtil.getMimeTypes(input, new MimeType(fallback))).toString
    }
  }
  def apply(input: InputStream): String = inputStreamMime(input)


  /**
   * Detects the mime type of a given file path.
   *
   * @param path The path for which to detect the mime type
   * @param fallback A fallback value in case no mime type can be found
   */
  def mimeType(path: String, fallback: String = DefaultMime) = {
    detectMime(fallback) {
      MimeUtil2.getMostSpecificMimeType(
        mimeUtil.getMimeTypes(path, new MimeType(fallback))).toString
    }
  }


  /**
   * Detects the mime type of a given url.
   *
   * @param url The url for which to detect the mime type
   * @param fallback A fallback value in case no mime type can be found
   */
  def urlMime(url: String, fallback: String = DefaultMime) = {
    detectMime(fallback) {
      MimeUtil2.getMostSpecificMimeType(
        mimeUtil.getMimeTypes(new URL(url), new MimeType(fallback))).toString
    }
  }
  def apply(uri: URI): String = urlMime(uri.toASCIIString)


  def isTextMime(mime: String): Boolean = MimeUtil2.isTextMimeType(new MimeType(mime))


  private def detectMime(fallback: String = DefaultMime)(mimeDetect: ⇒ String) = {
    def errorHandler(t: Throwable) = {
      logger.warn("There was an error detecting the mime type. ", t)
      fallback
    }
    allCatch.withApply(errorHandler)(mimeDetect)
  }




}

object MimeTypes extends Mimes