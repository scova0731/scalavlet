package org.scalavlet

import com.typesafe.config._

import scala.util.control.NonFatal
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit


/**
 * Configuration keeps configuration values retrieved by TypesafeHub/Config
 */
object Configuration {

  //TODO add exception handling for configuration loading
  private[this] val config = Configuration(ConfigFactory.load())

  def apply():Configuration = config

  private[Configuration] def asScalaList[A](l: java.util.List[A]): Seq[A] =
    asScalaBufferConverter(l).asScala.toList

  private[Configuration] def asScalaListConv[A, B](l: java.util.List[A])(converter: A => B): Seq[B] =
    asScalaBufferConverter(l).asScala.map(converter).toList

  //TODO arrange reference.conf
  val scalavletDevelopment:Boolean = config.getBoolean("scalavlet.development").getOrElse(true) // TODO dev-mode should  be false ?
  val scalavletAsyncTimeout:Long = config.getLong("scalavlet.async.timeout").getOrElse(60000L)
}


/**
 * A full configuration set.
 *
 * The underlying implementation is provided by https://github.com/typesafehub/config.
 *
 * Derived from play.api.Configuration
 *
 * @param underlying the underlying Config implementation
 */
case class Configuration(underlying: Config) {
  import Configuration._

  /**
   * Merge 2 configurations.
   */
  def ++(other: Configuration): Configuration = {
    Configuration(other.underlying.withFallback(underlying))
  }


  /**
   * Retrieves a configuration value as a `String`.
   *
   * This method supports an optional set of valid values:
   * {{{
   * val config = Configuration.load()
   * val mode = config.getString("engine.mode", Some(Set("dev","prod")))
   * }}}
   *
   * A configuration error will be thrown if the configuration value does not match any of the required values.
   *
   * @param path the configuration key, relative to configuration root key
   * @param validValues valid values for this configuration
   * @return a configuration value
   */
  def getString(path: String, validValues: Option[Set[String]] = None): Option[String] =
    readValue(path, underlying.getString(path)).map { value =>
      validValues match {
        case Some(values) if values.contains(value) => value
        case Some(values) if values.isEmpty => value
        case Some(values) => throw new ScalavletConfigException(path, s"Incorrect value, one of ${values.reduceLeft(_ + ", " + _)} was expected.")
        case None => value
      }
    }


  /**
   * Retrieves a configuration value as an `Int`.
   */
  def getInt(path: String): Option[Int] =
    readValue(path, underlying.getInt(path))


  /**
   * Retrieves a configuration value as a `Boolean`.
   */
  def getBoolean(path: String): Option[Boolean] =
    readValue(path, underlying.getBoolean(path))


  /**
   * Retrieves a configuration value as `Milliseconds`.
   */
  def getDuration(path: String, unit:TimeUnit): Option[Long] =
    readValue(path, underlying.getDuration(path, unit))


  /**
   * Retrieves a configuration value as `Bytes`.
   */
  def getByte(path: String): Option[Long] =
    readValue(path, underlying.getBytes(path))


  /**
   * Retrieves a sub-configuration, i.e. a configuration instance containing all keys starting with a given prefix.
   */
  def getConfig(path: String): Option[Configuration] =
    readValue(path, underlying.getConfig(path)).map(Configuration(_))


  /**
   * Retrieves a configuration value as a `Double`.
   */
  def getDouble(path: String): Option[Double] =
    readValue(path, underlying.getDouble(path))


  /**
   * Retrieves a configuration value as a `Long`.
   */
  def getLong(path: String): Option[Long] =
    readValue(path, underlying.getLong(path))


  /**
   * Retrieves a configuration value as a `Number`.
   */
  def getNumber(path: String): Option[Number] =
    readValue(path, underlying.getNumber(path))


  /**
   * Retrieves a configuration value as a Seq of `Boolean`.
   *
   * A configuration error will be thrown if the configuration value is not a valid `Boolean`.
   * Authorized vales are yes/no or true/false.
   */
  def getBooleanSeq(path: String): Option[Seq[java.lang.Boolean]] =
    readValue(path, underlying.getBooleanList(path)).map(asScalaList)


  /**
   * Retrieves a configuration value as a Seq of `Bytes`.
   */
  def getBytesSeq(path: String): Option[Seq[java.lang.Long]] =
    readValue(path, underlying.getBytesList(path)).map(asScalaList)


  /**
   * Retrieves a Seq of sub-configurations, i.e. a configuration instance for each key that matches the path.
   */
  def getConfigSeq(path: String): Option[Seq[Configuration]] =
    readValue[java.util.List[_ <: Config]](path, underlying.getConfigList(path)).
      map { configs => configs.asScala.map(Configuration(_)).asJava }.map(asScalaList)


  /**
   * Retrieves a configuration value as a Seq of `Double`.
   */
  def getDoubleSeq(path: String): Option[Seq[java.lang.Double]] =
    readValue(path, underlying.getDoubleList(path)).map(asScalaList)


  /**
   * Retrieves a configuration value as a Seq of `Integer`.
   */
  def getIntSeq(path: String): Option[Seq[java.lang.Integer]] =
    readValue(path, underlying.getIntList(path)).map(asScalaList)

  /**
   * Gets a list value (with any element type) as a ConfigList, which implements java.util.List<ConfigValue>.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val maxSizes = configuration.getList("engine.maxSizes")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.maxSizes = ["foo", "bar"]
   * }}}
   */
  def getList(path: String): Option[ConfigList] =
    readValue(path, underlying.getList(path))


  /**
   * Retrieves a configuration value as a Seq of `Long`.
   */
  def getLongSeq(path: String): Option[Seq[Long]] =
    readValue(path, underlying.getLongList(path)).map(asScalaListConv(_){j => j})


  /**
   * Retrieves a configuration value as Seq of `Milliseconds`.
   */
  def getDurationSeq(path: String, unit:TimeUnit): Option[Seq[Long]] =
    readValue(path, underlying.getDurationList(path, unit)).map(asScalaListConv(_){j=> j})


  /**
   * Retrieves a configuration value as a Seq of `Number`.
   */
  def getNumberSeq(path: String): Option[Seq[java.lang.Number]] =
    readValue(path, underlying.getNumberList(path)).map(asScalaList)


  /**
   * Retrieves a configuration value as a List of `ConfigObject`.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val engineProperties = configuration.getObjectList("engine.properties")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.properties = [{id: 5, power: 3}, {id: 6, power: 20}]
   * }}}
   */
  def getObjectList(path: String): Option[java.util.List[_ <: ConfigObject]] =
    readValue[java.util.List[_ <: ConfigObject]](path, underlying.getObjectList(path))


  /**
   * Retrieves a configuration value as a Seq of `String`.
   */
  def getStringSeq(path: String): Option[Seq[java.lang.String]] =
    readValue(path, underlying.getStringList(path)).map(asScalaList)

  /**
   * Retrieves a ConfigObject for this path, which implements Map<String,ConfigValue>
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val engineProperties = configuration.getObject("engine.properties")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.properties = {id: 1, power: 5}
   * }}}
   */
  def getObject(path: String): Option[ConfigObject] =
    readValue(path, underlying.getObject(path))


  /**
   * Returns available keys.
   *
   * @return the set of keys available in this configuration
   */
  def keys: Set[String] = underlying.entrySet.asScala.map(_.getKey).toSet


  /**
   * Returns sub-keys.
   *
   * @return the set of direct sub-keys available in this configuration
   */
  def subKeys: Set[String] = underlying.root().keySet().asScala.toSet


  /**
   * Returns every path as a set of key to value pairs, by recursively iterating through the
   * config objects.
   */
  def entrySet: Set[(String, ConfigValue)] =
    underlying.entrySet().asScala.map(e => e.getKey -> e.getValue).toSet


  /**
   * Returns every direct chile as a map of string key to value pairs
   */
  def childEntryMapOf(path:String): Map[String, Map[String, String]] =
    getConfig(path).map(cfg => {
      cfg.subKeys.map { childId =>
        val values = cfg.getConfig(childId).map(cfg => cfg.keys.map {
          case (key) => key -> cfg.getString(key).get
        })
        childId -> values.getOrElse(Set()).toMap
      }.toSeq
    }).getOrElse(Set()).toMap


  /**
   * Read a value from the underlying implementation,
   * catching Errors and wrapping it in an Option value.
   */
  private def readValue[T](path: String, v: => T): Option[T] = {
    try {
      Option(v)
    } catch {
      case e: ConfigException.Missing => None
      case NonFatal(e) => throw new ScalavletConfigException(path, e.getMessage, e)
    }
  }
}