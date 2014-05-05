package org.scalavlet.utils

/**
 * Mixing-in key-value cache
 *
 * This cache doesn't assume "multi-threading" access
 */
trait Cacheable {
  private var internalCache = Map[String, Any]()

  def cache(key:String):Any =
    internalCache(key)

  def cache_=(map:Map[String, Any]) =
    internalCache = map

  def cacheOrElse(key:String):Option[Any] =
    internalCache.get(key)

  def cacheAs[A](key:String):Any =
    internalCache(key).asInstanceOf[A]

  def cacheAsOrElse[A](key:String):Option[A] =
    internalCache.get(key).map(_.asInstanceOf[A])

  /**
   * Return the attribute associated with the key or throw an exception when nothing found
   *
   * @param key The key to find
   * @tparam A The type of the value
   * @return an value for the attributed associated with the key in the underlying servlet object,
   *         or throw an exception if the key doesn't exist
   */
  def cacheOrDefault[A](key: String, default: => A): A =
    cacheAsOrElse[A](key) getOrElse default


  def addCache(key:String, value:Any):Cacheable = {
    internalCache = internalCache + (key -> value)
    this
  }

  def addCache(kv:(String, Any)):Cacheable = {
    internalCache = internalCache + kv
    this
  }

  def removeCache(key:String):Cacheable = {
    internalCache = internalCache - key
    this
  }

  def cacheContains(key:String):Boolean =
    internalCache.contains(key)
}

class Cache extends Cacheable

object Cache {
  def apply():Cache = new Cache
}
