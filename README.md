# Disclaimer
**This is continuation of Leon's [play-salat plugin](https://github.com/leon/play-salat) for future versions of Play and is comptabile with latest version of play 2.5.3. Compatibility with earlier versions is not checked, please use older artifacts for older versions of Play Framework. Update your Build.scala or build.sbt with artifactId mentioned below.**

# MongoDB Salat plugin for Play Framework 2
Salat is a ORM for MongoDBs scala driver called Casbah.

The plugin's functionality simpifies the use of salat by presenting a simple "play style" configuration and binders for `ObjectId`

 * https://github.com/mongodb/casbah
 * https://github.com/salat/salat

 [![Build Status](https://travis-ci.org/shayanlinux/play-salat.svg?branch=master)](https://travis-ci.org/shayanlinux/play-salat)


## Installation

Start by adding the plugin, in your `project/Build.scala`
````scala
val appDependencies = Seq(
  "com.github.shayanlinux" %% "play-plugins-salat" % "1.6.0"
)
````
Then we can add the implicit converstions to and from ObjectId by adding to the routesImport and add ObjectId to all the templates

####Play 2.3.x and subsequent

````scala
import play.twirl.sbt.Import.TwirlKeys
val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
  routesImport += "se.radley.plugin.salat.Binders._",
  TwirlKeys.templateImports += "org.bson.types.ObjectId"
)
````

We continue to edit the `conf/application.conf` file. We need to disable some plugins that we don't need.
Add these lines:

    dbplugin = disabled
    evolutionplugin = disabled
    ehcacheplugin = disabled
    
**Only for Play 2.3.x and 2.4.x**

We now need to register the plugin, this is done by creating(or appending) to the `conf/play.plugins` file

    500:se.radley.plugin.salat.SalatPlugin
    
**Only for Play 2.5.x+**

Use version `1.6.0` that provides Play-Salat __not__ as a _plugin_ but as a _Guicable module_.

````scala
val appDependencies = Seq(
  "com.github.shayanlinux" %% "play-plugins-salat" % "1.6.0"
)
````

Add the following line to the `conf/application.conf` file. This will enable the Play-Salat module and Guice will inject it.

	play.modules.enabled  += "se.radley.plugin.salat.PlaySalatModule"

## Configuration
now we need to setup our connections. The plugin is modeled after how plays DB plugin is built.

    mongodb.default.db = "mydb"
    # Optional values
    #mongodb.default.host = "127.0.0.1"
    #mongodb.default.port = 27017
    #mongodb.default.user = "leon"
    #mongodb.default.password = "123456"

	# MongoURI
	# ~~~~~
	# a MongoURI can also be used http://www.mongodb.org/display/DOCS/Connections
	# mongodb.default.uri = "mongodb://127.0.0.1:27017,mongodb.org:1337/salat-test"

	# WriteConcern
	# ~~~~~
	# Can be any of the following
	#
	# fsyncsafe - Exceptions are raised for network issues and server errors; Write operations wait for the server to flush data to disk.
	# replicassafe - Exceptions are raised for network issues and server errors; waits for at least 2 servers for the write operation.
	# safe - Exceptions are raised for network issues and server errors; waits on a server for the write operation.
	# normal - Exceptions are raised for network issues but not server errors.

	#mongodb.default.writeconcern = "safe"

	# Replica sets
	# ~~~~~
	# http://www.mongodb.org/display/DOCS/Why+Replica+Sets
	#
	# To user a replicaset instead of a single host, omit optional values and use the configuration below instead.
	# Since replica sets use public key authentication, user and password won't work together with the replicaset option.

	#mongodb.default.replicaset {
	#    host1.host = "10.0.0.1"
	#
	#    host2.host = "10.0.0.2"
	#    host2.port = 27018
	#}

	# Mongo Options
	# ~~~~~
	# http://api.mongodb.org/java/2.8.0/com/mongodb/MongoOptions.html
	#
	# For passing custom options to the MongoConnection add the properties under "options". Add just the ones which are different from defaults.

	#mongodb.default.options {
	#    connectionsPerHost = 100
	#    threadsAllowedToBlockForConnectionMultiplier = 1000
	#    connectTimeout = 60000
	#}

## More that one DB?
If you would like to connect to two databases you need to create two source names. You also can specify different options per database

    mongodb.myotherdb.db = "otherdb"
    mongodb.myotherdb.options.connectionsPerHost = 80

Then you can call `playSalat.collection("collectionname", "myotherdb")`

## What a model looks like
All models must be case classes otherwise salat doesn't know how to properly transform them into MongoDBObject's

````scala
package models

import java.util.Date
import com.novus.salat.annotations._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import se.radley.plugin.salat.Binders._

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  password: String,
  address: List[Address] = Nil,
  added: Date = new Date(),
  updated: Option[Date] = None,
  @Key("company_id") company: Option[ObjectId] = None)
````

## Dependency Injection

Modules managed by the _Guice dependency injection framework_ are declared like this:

````scala
import com.google.inject.AbstractModule
import services.{ UserDAO, MongoContext }

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[MongoContext])
    bind(classOf[UserDAO])
  }
}
````

## Collections

A _Salat Mongo Context_ is typically implemented like this:

````scala
package services

import javax.inject._
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
````

A DAO to a collection with _Salat Mongo Context_ and _Play Salat Plugin_ injected is implemented like this:

````scala
package services

import javax.inject._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.PlaySalat
import models.User

@Singleton
class UserDAO @Inject() (playSalat: PlaySalat, mongoContext: MongoContext) extends ModelCompanion[User, ObjectId] {
  import mongoContext._

  def collection = playSalat.collection("users")
  val dao = new SalatDAO[User, ObjectId](collection) {}

  // Queries
  def findOneByUsername(username: String): Option[User] =
    dao.findOne(MongoDBObject("username" -> username))

  def findByCountry(country: String) =
    dao.find(MongoDBObject("address.country" -> country))
}
````

## Capped Collections

If you want to use capped collections check this out.

Model:

````scala
package models

import java.util.Date
import com.mongodb.casbah.Imports._

case class LogItem(
  id: ObjectId = new ObjectId,
  message: String)
````

DAO (also remember to register it in the _Module_ class):

````scala
package services

import javax.inject._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.PlaySalat
import models.LogItem

@Singleton
class LogItemDAO @Inject() (playSalat: PlaySalat, mongoContext: MongoContext) extends ModelCompanion[LogItem, ObjectId] {
  import mongoContext._

  val collection = playSalat.cappedCollection("logitems",
    1048576, // max size is 1 MB = 10485760 bytes
    Some(10)) // max number of documents = 10
  val dao = new SalatDAO[LogItem, ObjectId](collection) {}
}
````

## GridFS
__TODO__ adjust the example to use dependency injection.

If you want to store things in gridfs you can do this

````
package models

import play.api.Play.current
import se.radley.plugin.salat._
import mongoContext._

val files = gridFS("myfiles")
````

## Mongo Context
All models must contain an implicit salat Context. The context is somewhat like a hibernate dialect.
You can override mapping names and configure how salat does it's type hinting. read more about it [here](https://github.com/salat/salat/wiki/CustomContext)

In the sample there is a custom `mongoContext`, partly because we need to add plays classloader to salat so it knows when to reload it's graters,
but also so we can override all models id fields to be serialized to MongoDB's _id.

- [Sample](https://github.com/shayanlinux/play-salat/tree/master/sample)

## Enums?
If you're using Scala Enumerations have a look at my play-enumeration project.

- [play-enumeration](https://github.com/leon/play-enumeration)
