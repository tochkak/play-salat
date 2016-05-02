import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import models._
import se.radley.plugin.salat._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if (User.count(DBObject(), Nil, Nil) == 0) {
      Logger.info("Loading Testdata")
      User.save(User(
        username = "leon",
        password = "1234",
        address = List(
          Address("Örebro", "123 45", "Sweden"),
          Address("Amsterdam", "1234", "The Netherlands"))))

      User.save(User(
        username = "guillaume",
        password = "1234",
        address = List(
          Address("Paris", "75000", "France"))))
    }
  }

}
