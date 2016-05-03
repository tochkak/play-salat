package models

import javax.inject._
import com.novus.salat.dao._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }
import play.api.Environment

@Singleton
class MongoContext @Inject() (environment: Environment) {

  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(environment.classLoader)
    context
  }
}
