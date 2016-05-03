import com.google.inject.AbstractModule
import models.{ MongoContext, UserDAO }

class Module extends AbstractModule {

  override def configure() = {
    
    bind(classOf[MongoContext])
    bind(classOf[UserDAO])
    bind(classOf[InitializeDatabase]).asEagerSingleton
    
  }

}