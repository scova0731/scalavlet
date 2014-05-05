package org.scalavlet.test

import java.io.InputStream
import collection.DefaultMap


abstract class ClientResponse {
  def bodyBytes: Array[Byte]
  def inputStream: InputStream
  def statusLine: ResponseStatus
  def headers: Map[String, Seq[String]]

  def body: String = new String(bodyBytes, charset.getOrElse("ISO-8859-1"))

  def mediaType: Option[String] = {
    _header.get("Content-Type") match {
      case Some(contentType) => contentType.split(";").map(_.trim).headOption
      case _ => None
    }
  }

  def status: Int = statusLine.code

  def charset: Option[String] = {
    _header.getOrElse("Content-Type", "").split(";").map(_.trim).find(_.startsWith("charset=")) match {
      case Some(attr) => Some(attr.split("=")(1))
      case _          => None
    }
  }

  def reason: String = statusLine.message

  def header(name: String): String = _header.getOrElse(name, null)

  def longHeader(name: String): Long = _header.getOrElse(name, "-1").toLong

  def contentType: String = _header.getOrElse("Content-Type", null)

  private val _header = new DefaultMap[String, String] {
    def get(key: String) = {
      headers.get(key) match {
        case Some(values) => Some(values.head)
        case _            => None
      }
    }

    override def apply(key: String) = {
      get(key) match {
        case Some(value) => value
        case _           => null
      }
    }

    def iterator = {
      headers.keys.map(name => name -> this(name)).iterator
    }
  }
}
