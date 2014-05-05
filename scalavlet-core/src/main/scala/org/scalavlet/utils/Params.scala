package org.scalavlet.utils

import scala.collection.immutable.Map
import scala.reflect.ClassTag
import java.util.Date
import java.text.SimpleDateFormat


class Params(multiMap: Map[String, Seq[String]])
  extends Map[String, String] {

  override def get(key: String): Option[String] =
    multiMap.get(key) flatMap { _.headOption }

  //TODO this doesn't work
  def getAs[A](key: String)(implicit ct: ClassTag[A]):Option[A] =
    try {
      ct.runtimeClass.getName match {
        case "int" => get(key).map(_.toInt.asInstanceOf[A])
        case v => None //get(key).map(_.asInstanceOf[A])
      }
    }
    catch { case ex:Throwable => None }

  def getAs[T <: Date](nameAndFormat: (String, String)): Option[Date] =
    get(nameAndFormat._1).map(new SimpleDateFormat(nameAndFormat._2).parse)

  def getAsInt(key: String):Option[Int] =
    try { get(key).map(_.toInt) } catch { case ex:Throwable => None }


  override def size =
    multiMap.size

  override def iterator =
    multiMap.map { case(k, v) => (k, v.head) }.iterator

  override def -(key: String) =
    Map() ++ this - key

  override def +[B1 >: String](kv: (String, B1)): Map[String, B1] =
    Map() ++ this + kv

  //TODO add getInt(key:String):Option[Int] and several major conversions
  //TODO add getIntOrElse(key:String, value:Int):Int and several major

}


object Params {
  
  def apply(multiMap: Map[String, Seq[String]]):Params =
    new Params(multiMap)
}
