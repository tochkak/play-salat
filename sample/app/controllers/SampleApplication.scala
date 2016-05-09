package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.{ UserDAO, LogItemDAO }
import com.mongodb.casbah.Imports._
import models.LogItem

@Singleton
class SampleApplication @Inject() (userDAO: UserDAO, logitemDAO: LogItemDAO) extends Controller {

  def list() = Action {
    val users = userDAO.findAll
    Ok(views.html.list(users))
  }

  def view(id: ObjectId) = Action { request =>
    userDAO.findOneById(id).map { user =>
      val li = LogItem(
        remoteIP = request.remoteAddress,
        remoteAgent = request.headers.toMap.get("User-Agent").get.mkString(" --- "),
        message = s"Access to User information with ID: $id")
      logitemDAO.insert(li)
      Ok(views.html.user(user))
    }.getOrElse(NotFound)
  }
}
