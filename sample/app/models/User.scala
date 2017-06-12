package models

import java.util.Date

import org.bson.types.ObjectId
import ru.tochkak.plugin.salat.Binders.objectIdReads
import play.api.libs.functional.syntax._
import play.api.libs.json._
import salat.annotations.Key

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  password: String,
  address: List[Address] = Nil,
  added: Date = new Date(),
  updated: Option[Date] = None,
  @Key("company_id") company: Option[ObjectId] = None
)

object User extends UserJson

/** Trait used to convert to and from json */
trait UserJson {

  implicit val userJsonWrite = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id.toString,
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
      (__ \ 'company).readNullable[ObjectId]) (User.apply _)
}
