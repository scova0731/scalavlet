package org.scalavlet.utils

import scala.collection.immutable.Map
import scala.reflect.ClassTag
import java.util.Date
import java.text.SimpleDateFormat
// This is added based on the advise of compiler warning
import scala.language.implicitConversions

object MultiParams {
  def apply() =
    new MultiParams(Map.empty)

  def apply[SeqType <: Seq[String]](wrapped: Map[String, SeqType]) =
    new MultiParams(wrapped)

  def empty =
    apply()

  implicit def map2MultiMap(map: Map[String, Seq[String]]) = new MultiParams(map)
}

class MultiParams(underlying: Map[String, Seq[String]]) extends Map[String, Seq[String]] {

  override def apply(key: String): Seq[String] =
    underlying.get(key).getOrElse(Seq())

  def get(key: String): Option[Seq[String]] =
    underlying.get(key) orElse underlying.get(key + "[]")

  def getAs[A](key: String)(implicit ct: ClassTag[A]): Option[Seq[A]] =
    underlying.get(key).map(_.map(_.asInstanceOf[A]))

  def getAs[T <: Date](nameAndFormat: (String, String)): Option[Date] =
    get(nameAndFormat._1).map(d => new SimpleDateFormat(nameAndFormat._2).parse(d(0)))

  def getAsInt(key: String):Option[Seq[Int]] =
    try { get(key).map(_.map(_.toInt)) } catch { case ex:Throwable => None }

  def get(key: Symbol): Option[Seq[String]] =
    get(key.name)

  def +[B1 >: Seq[String]](kv: (String, B1)): MultiParams =
    new MultiParams(underlying + kv.asInstanceOf[(String, Seq[String])])

  def -(key: String): MultiParams =
    new MultiParams(underlying - key)

  def iterator: Iterator[(String, Seq[String])] =
    underlying.iterator

  override def default(a: String): Seq[String] =
    underlying.default(a)
}
