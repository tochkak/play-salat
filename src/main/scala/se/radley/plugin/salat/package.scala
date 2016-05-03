package se.radley.plugin

import play.api._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.gridfs.GridFS
import scala.util.{ Try, Success, Failure }

package object salat {

  /**
   * Returns a MongoCollection
   * @param collectionName The MongoDB collection name
   * @param sourceName The configured source name
   * @return MongoCollection
   */
  def mongoCollection(
    collectionName: String,
    sourceName: String = "default")(implicit app: Application): MongoCollection = {
    Try(app.injector.instanceOf[SalatComponent]) match {
      case Success(salat) => salat.collection(collectionName, sourceName)
      case Failure(ex) => throw new PlayException("SalatPlugin is not registered.",
        "You need to register the plugin with \"play.modules.enabled  += \"se.radley.plugin.salat.SalatModule\"\" in conf/application.conf")
    }
  }

  /**
   * Returns a capped MongoCollection
   * @param collectionName The MongoDB collection name
   * @param size The capped collection size
   * @param max the capped collection max number of documents
   * @param sourceName The configured source name
   * @return MongoCollection
   */
  def mongoCappedCollection(
    collectionName: String,
    size: Long,
    max: Option[Long] = None,
    sourceName: String = "default")(implicit app: Application): MongoCollection = {
    Try(app.injector.instanceOf[SalatComponent]) match {
      case Success(salat) => salat.cappedCollection(collectionName, size, max, sourceName)
      case Failure(ex) => throw new PlayException("SalatPlugin is not registered.",
        "You need to register the plugin with \"play.modules.enabled  += \"se.radley.plugin.salat.SalatModule\"\" in conf/application.conf")
    }
  }

  /**
   * Returns a GridFS bucket
   * @param bucketName The GridFS bucket name
   * @param sourceName The configured source name
   * @return GridFS
   */
  def gridFS(
    bucketName: String,
    sourceName: String = "default")(implicit app: Application): GridFS = {
    Try(app.injector.instanceOf[SalatComponent]) match {
      case Success(salat) => salat.gridFS(bucketName, sourceName)
      case Failure(ex) => throw new PlayException("SalatPlugin is not registered.",
        "You need to register the plugin with \"play.modules.enabled  += \"se.radley.plugin.salat.SalatModule\"\" in conf/application.conf")
    }
  }

}
