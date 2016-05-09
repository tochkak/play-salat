package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.UserDAO
import com.mongodb.casbah.Imports._

@Singleton
class SampleApplication @Inject() (userDAO: UserDAO) extends Controller {

  def list() = Action {
    val users = userDAO.findAll
    Ok(views.html.list(users))
  }

  def view(id: ObjectId) = Action {
    userDAO.findOneById(id).map(user =>
      Ok(views.html.user(user))).getOrElse(NotFound)
  }
}
