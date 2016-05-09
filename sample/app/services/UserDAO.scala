package services

import javax.inject._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.PlaySalat
import models.{ Address, User }

@Singleton
class UserDAO @Inject() (playSalat: PlaySalat, mongoContext: MongoContext) extends ModelCompanion[User, ObjectId] {
  import mongoContext._

  val collection = playSalat.collection("users")
  val dao = new SalatDAO[User, ObjectId](collection) {}

  // Indexes
  collection.createIndex(DBObject("username" -> 1), DBObject("name" -> "user_email", "unique" -> true))

  // Queries
  def findOneByUsername(username: String): Option[User] =
    dao.findOne(MongoDBObject("username" -> username))

  def findByCountry(country: String) =
    dao.find(MongoDBObject("address.country" -> country))

  def authenticate(username: String, password: String): Option[User] =
    findOne(DBObject("username" -> username, "password" -> password))

  def addresses(username: String): List[Address] =
    findOneByUsername(username).map { user =>
      user.address
    } getOrElse {
      Nil
    }
}