import com.google.inject.AbstractModule
import services._

class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[MongoContext])
    bind(classOf[UserDAO])
    bind(classOf[LogItemDAO])
    bind(classOf[InitializeDatabase]).asEagerSingleton

  }

}
