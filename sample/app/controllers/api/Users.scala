package controllers.api

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import controllers.Actions._

import com.mongodb.casbah.WriteConcern
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._

@Singleton
class Users @Inject() extends Controller {

  def index(country: Option[String]) = Action {
    country match {
      case Some(country) => Ok(Json.toJson(User.findByCountry(country).toList))
      case None => Ok(Json.toJson(User.findAll().toList))
    }
  }

  def create = JsonAction[User] { user =>
    User.save(user, WriteConcern.Safe)
    Ok(Json.toJson(user))
  }

  def view(id: ObjectId) = Action {
    User.findOneById(id).map { user =>
      Ok(Json.toJson(user))
    } getOrElse {
      NotFound
    }
  }

  def update(id: ObjectId) = JsonAction[User] { requestUser =>
    val user = requestUser.copy(id)
    User.save(user, WriteConcern.Safe)
    Ok(Json.toJson(user))
  }

  def delete(id: ObjectId) = Action {
    User.removeById(id)
    Ok("")
  }
  
  def addresses(username: String, address: Option[Boolean]) = Action {
    address match {
      case Some(true) => Ok(Json.toJson(User.addresses(username)))
      case _ => Ok(Json.toJson(User.findOneByUsername(username)))
    }
  }
}
