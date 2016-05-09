import com.google.inject.AbstractModule
import services.{ UserDAO, MongoContext }

class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[MongoContext])
    bind(classOf[UserDAO])
    bind(classOf[InitializeDatabase]).asEagerSingleton

  }

}
