package ru.tochkak.plugin.salat

import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

import com.mongodb._
import play.api.Configuration

object OptionsFromConfig {

  private def getInstanceFromName[T](name: String): Option[T] = {
    try {
      Some(Class.forName(name).newInstance().asInstanceOf[T])
    } catch {
      case ex: ClassNotFoundException => None
    }
  }

  def apply(config: Configuration): Option[com.mongodb.MongoClientOptions] = {
    if (config.keys.isEmpty) {
      return None
    }

    val builder = new MongoClientOptions.Builder()

    config.getOptional[Int]("connectionsPerHost").map(v => builder.connectionsPerHost(v))
    config.getOptional[Int]("connectTimeout").map(v => builder.connectTimeout(v))
    config.getOptional[Boolean]("cursorFinalizerEnabled").map(v => builder.cursorFinalizerEnabled(v))
    config.getOptional[String]("dbDecoderFactory").flatMap(
      className => getInstanceFromName[DBDecoderFactory](className)).map(v => builder.dbDecoderFactory(v)
    )
    config.getOptional[String]("dbEncoderFactory").flatMap(
      className => getInstanceFromName[DBEncoderFactory](className)).map(v => builder.dbEncoderFactory(v)
    )
    config.getOptional[String]("description").map(v => builder.description(v))
    config.getOptional[Int]("maxWaitTime").map(v => builder.maxWaitTime(v))
    config.getOptional[String]("readPreference").flatMap { name =>
      try {
        Some(ReadPreference.valueOf(name))
      } catch {
        case _: IllegalArgumentException => None
      }
    }.map(v => builder.readPreference(v))
    config.getOptional[String]("socketFactory").flatMap(
      className => getInstanceFromName[SocketFactory](className)
    ).map(v => builder.socketFactory(v))
    config.getOptional[Boolean]("socketKeepAlive").map(v => builder.socketKeepAlive(v))
    config.getOptional[Int]("socketTimeout").map(v => builder.socketTimeout(v))
    config.getOptional[Int]("threadsAllowedToBlockForConnectionMultiplier").map(v => builder.threadsAllowedToBlockForConnectionMultiplier(v))
    config.getOptional[String]("writeConcern").map(name => WriteConcern.valueOf(name)).map(v => builder.writeConcern(v))
    config.getOptional[Boolean]("ssl").map(v => if (v) builder.socketFactory(SSLSocketFactory.getDefault))

    Some(builder.build())
  }
}