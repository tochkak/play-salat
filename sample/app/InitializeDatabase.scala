import javax.inject._
import com.mongodb.casbah.Imports._
import play.api._
import models._
import se.radley.plugin.salat._

@Singleton
class InitializeDatabase @Inject() (userDAO: UserDAO) {
  if (userDAO.count(DBObject(), Nil, Nil) == 0) {
    Logger.info("Loading Testdata")
    userDAO.save(User(
      username = "leon",
      password = "1234",
      address = List(
        Address("Örebro", "123 45", "Sweden"),
        Address("Amsterdam", "1234", "The Netherlands"))))

    userDAO.save(User(
      username = "guillaume",
      password = "1234",
      address = List(
        Address("Paris", "75000", "France"))))
  }
}
