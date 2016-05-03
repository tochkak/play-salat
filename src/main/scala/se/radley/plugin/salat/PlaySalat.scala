package se.radley.plugin.salat

import play.api._
import com.mongodb.casbah._
import com.mongodb.{ MongoClientOptions, MongoException, ServerAddress, MongoOptions }
import com.mongodb.casbah.gridfs.GridFS
import commons.MongoDBObject
import scala.util.Try

trait PlaySalat {
  val configuration: Configuration

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
          options.map(MongoClient(hosts, credentials, _)).
            getOrElse(MongoClient(hosts, credentials))).getOrElse {
            throw configuration.reportError(
              "mongodb",
              s"Access denied to MongoDB database: [$dbName] with user: [${user.getOrElse("")}]")
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

  val sources: Map[String, MongoSource]

  /**
   * Returns the MongoSource that has been configured in application.conf
   * @param source The source name ex. default
   * @return A MongoSource
   */
  def source(source: String): MongoSource

  /**
   * Returns MongoDB for configured source
   * @param sourceName The source name ex. default
   * @return A MongoDB
   */
  def db(sourceName: String = "default"): MongoDB

  /**
   * Returns MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def collection(collectionName: String, sourceName: String = "default"): MongoCollection

  /**
   * Returns Capped MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param size The capped collection size
   * @param max The capped collection max number of documents
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def cappedCollection(collectionName: String,
                       size: Long,
                       max: Option[Long] = None,
                       sourceName: String = "default"): MongoCollection

  /**
   * Returns GridFS for configured source
   * @param bucketName The bucketName for the GridFS instance
   * @param sourceName The source name ex. default
   * @return A GridFS
   */
  def gridFS(bucketName: String = "fs", sourceName: String = "default"): GridFS

}