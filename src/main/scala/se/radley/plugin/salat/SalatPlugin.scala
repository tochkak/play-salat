package se.radley.plugin.salat

import play.api._
import play.api.mvc._
import play.api.Play.current
import com.mongodb.casbah._
import com.mongodb.{ MongoClientOptions, MongoException, ServerAddress, MongoOptions }
import com.mongodb.casbah.gridfs.GridFS
import commons.MongoDBObject
import com.mongodb.casbah.MongoClientOptions
import scala.util.Try
import javax.inject.Inject

class SalatPlugin @Inject() (implicit app: Application) extends Plugin {

  lazy val configuration = app.configuration.getConfig("mongodb").getOrElse(Configuration.empty)

  case class MongoSource(
      val hosts: List[ServerAddress],
      val dbName: String,
      val writeConcern: com.mongodb.WriteConcern,
      val user: Option[String] = None,
      val password: Option[String] = None,
      val options: Option[MongoClientOptions],
      private var conn: MongoClient = null) {

    def connection: MongoClient = {
      if (conn == null) {

        val credentials = {
          val maybe = for {
            u <- user
            p <- password
          } yield MongoCredential.createCredential(u, dbName, p.toArray)

          List(maybe).flatten
        }

        conn = Try( 
          options.map(MongoClient(hosts, credentials, _)).getOrElse(MongoClient(hosts, credentials))
        ).getOrElse {
          throw configuration.reportError("mongodb", s"Access denied to MongoDB database: [$dbName] with user: [${user.getOrElse("")}]")
        }

        conn.setWriteConcern(writeConcern)
      }
      conn
    }

    def reset() {
      conn.close()
      conn = null
    }

    def db: MongoDB = connection(dbName)

    def collection(name: String): MongoCollection = db(name)

    def cappedCollection(name: String, size: Long, max: Option[Long] = None): MongoCollection = {
      val coll = if (db.collectionExists(name)) {
        db(name)
      } else {
        val options = MongoDBObject.newBuilder
        options += "capped" -> true
        options += "size" -> size
        if (max.isDefined)
          options += "max" -> max.get
        new MongoCollection(db.createCollection(name, options.result()))
      }
      coll
    }

    def gridFS(bucketName: String = "fs"): GridFS = GridFS(db, bucketName)

    override def toString() = {
      (if (user.isDefined) user.get + "@" else "") +
        hosts.map(h => h.getHost + ":" + h.getPort).mkString(", ") +
        "/" + dbName + options.map(" with Options[" + _ + "]").getOrElse("")
    }
  }

  lazy val sources: Map[String, MongoSource] = configuration.subKeys.map { sourceKey =>
    val source = configuration.getConfig(sourceKey).getOrElse(Configuration.empty)
    val options: Option[MongoClientOptions] = source.getConfig("options").flatMap(opts => OptionsFromConfig(opts))

    source.getString("uri").map { str =>
      // MongoURI config - http://www.mongodb.org/display/DOCS/Connections
      val uri = MongoClientURI(str)
      val hosts = uri.hosts.map { host =>
        if (host.contains(':')) {
          val Array(h, p) = host.split(':')
          new ServerAddress(h, p.toInt)
        } else {
          new ServerAddress(host)
        }
      }.toList
      val db = uri.database.getOrElse(throw configuration.reportError("mongodb." + sourceKey + ".uri", "db missing for source[" + sourceKey + "]"))
      val writeConcern = uri.options.getWriteConcern
      val user = uri.username
      val password = uri.password.map(_.mkString).filterNot(_.isEmpty)
      sourceKey -> MongoSource(hosts, db, writeConcern, user, password, options)
    }.getOrElse {
      val dbName = source.getString("db").getOrElse(throw configuration.reportError("mongodb." + sourceKey + ".db", "db missing for source[" + sourceKey + "]"))

      // Simple config
      val host = source.getString("host").getOrElse("127.0.0.1")
      val port = source.getInt("port").getOrElse(27017)
      val user: Option[String] = source.getString("user")
      val password: Option[String] = source.getString("password")

      // Replica set config
      val hosts: List[ServerAddress] = source.getConfig("replicaset").map { replicaset =>
        replicaset.subKeys.map { hostKey =>
          val c = replicaset.getConfig(hostKey).get
          val host = c.getString("host").getOrElse(throw configuration.reportError("mongodb." + sourceKey + ".replicaset", "host missing for replicaset in source[" + sourceKey + "]"))
          val port = c.getInt("port").getOrElse(27017)
          new ServerAddress(host, port)
        }.toList.reverse
      }.getOrElse(List.empty)

      val writeConcern =
        source.getString("writeconcern", Some(Set("fsyncsafe", "replicassafe", "safe", "normal")))
          .flatMap(WriteConcern.valueOf)
          .getOrElse(WriteConcern.Safe)

      // If there are replicasets configured go with those otherwise fallback to simple config
      if (hosts.isEmpty)
        sourceKey -> MongoSource(List(new ServerAddress(host, port)), dbName, writeConcern, user, password, options)
      else
        sourceKey -> MongoSource(hosts, dbName, writeConcern, user, password, options)
    }
  }.toMap

  override def enabled = !configuration.subKeys.isEmpty

  override def onStart() {
    sources.map { source =>
      app.mode match {
        case Mode.Test =>
        case _ => {
          try {
            source._2.connection(source._2.dbName).getCollectionNames()
          } catch {
            case e: MongoException =>
              Logger("play").debug("error: " + e.printStackTrace)

              throw configuration.reportError("mongodb." + source._1, "couldn't connect to [" + source._2.hosts.mkString(", ") + "]", Some(e))
          } finally {
            Logger("play").info("mongodb [" + source._1 + "] connected at " + source._2)
          }
        }
      }
    }
  }

  override def onStop() {
    sources.map { source =>
      // @fix See if we can get around the plugin closing connections in testmode
      if (app.mode != Mode.Test)
        source._2.reset()
    }
  }

  /**
   * Returns the MongoSource that has been configured in application.conf
   * @param source The source name ex. default
   * @return A MongoSource
   */
  def source(source: String): MongoSource = {
    sources.get(source).getOrElse(throw configuration.reportError("mongodb." + source, source + " doesn't exist"))
  }

  /**
   * Returns MongoDB for configured source
   * @param sourceName The source name ex. default
   * @return A MongoDB
   */
  def db(sourceName: String = "default"): MongoDB = source(sourceName).db

  /**
   * Returns MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def collection(collectionName: String, sourceName: String = "default"): MongoCollection = source(sourceName).collection(collectionName)

  /**
   * Returns Capped MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param size The capped collection size
   * @param max The capped collection max number of documents
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def cappedCollection(collectionName: String, size: Long, max: Option[Long] = None, sourceName: String = "default"): MongoCollection = source(sourceName).cappedCollection(collectionName, size, max)

  /**
   * Returns GridFS for configured source
   * @param bucketName The bucketName for the GridFS instance
   * @param sourceName The source name ex. default
   * @return A GridFS
   */
  def gridFS(bucketName: String = "fs", sourceName: String = "default"): GridFS = source(sourceName).gridFS(bucketName)
}
