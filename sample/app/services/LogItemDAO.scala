package services

import javax.inject._

import com.mongodb.casbah.Imports._
import models.LogItem
import ru.tochkak.plugin.salat.PlaySalat
import salat.dao.{ModelCompanion, SalatDAO}

@Singleton
class LogItemDAO @Inject()(playSalat: PlaySalat, mongoContext: MongoContext) extends ModelCompanion[LogItem, ObjectId] {

  import mongoContext._

  val collection = playSalat.cappedCollection("logitems",
    1048576, // max size is 1 MB = 10485760 bytes
    Some(10)) // max number of documents = 10
  val dao = new SalatDAO[LogItem, ObjectId](collection) {}
}
