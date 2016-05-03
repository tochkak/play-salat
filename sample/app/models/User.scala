package models

import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import se.radley.plugin.salat.Binders._
import se.radley.plugin.salat.PlaySalat
import javax.inject._

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  password: String,
  address: List[Address] = Nil,
  added: Date = new Date(),
  updated: Option[Date] = None,
  @Key("company_id") company: Option[ObjectId] = None)

object User extends UserJson

@Singleton
class UserDAO @Inject() (playSalat: PlaySalat, mongoContext: MongoContext) extends ModelCompanion[User, ObjectId] {
  import mongoContext._

  def collection = playSalat.collection("users")
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

/** Trait used to convert to and from json */
trait UserJson {

  implicit val userJsonWrite = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id,
        "username" -> u.username,
        "address" -> u.address,
        "added" -> u.added,
        "updated" -> u.updated)
    }
  }

  implicit val userJsonRead = (
    (__ \ 'id).read[ObjectId] ~
    (__ \ 'username).read[String] ~
    (__ \ 'password).read[String] ~
    (__ \ 'address).read[List[Address]] ~
    (__ \ 'added).read[Date] ~
    (__ \ 'updated).readNullable[Date] ~
    (__ \ 'company).readNullable[ObjectId])(User.apply _)
}
