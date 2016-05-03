package se.radley.plugin.salat

import org.specs2.mutable.Specification
import play.api._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import java.io.File
import com.mongodb.casbah._
import com.mongodb.ServerAddress
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import scala.util.Try

object SalatSpec extends Specification {

  lazy val salatAppBuilder = GuiceApplicationBuilder()
    .bindings(bind[PlaySalat].to[PlaySalatImpl])

  "Salat Plugin with basic config" should {

    lazy val app = salatAppBuilder.configure(Map(
      ("mongodb.default.db" -> "salat-test"),
      ("mongodb.default.writeconcern" -> "normal"))).build

    lazy val salat = app.injector.instanceOf[PlaySalat]

    running(app) {
      "start" in {
        salat must beAnInstanceOf[PlaySalat]
      }

      "return a MongoCollection" in {
        val col = salat.collection("salat-collection")
        col must beAnInstanceOf[MongoCollection]
      }

      "set write concern" in {
        val source = salat.source("default")
        source.writeConcern must equalTo(WriteConcern.Normal)
      }

      "fail if source doesn't exist" in {
        salat.collection("salat-collection", "sourcethatdoesntexist") must throwAn[PlayException]
      }
    }

    "be disabled if no configuration exists" in {
      val app = GuiceApplicationBuilder().build
      running(app) {
        Try(app.injector.instanceOf[PlaySalat]) must beFailedTry
      }
    }
  }

  "Salat Plugin with uri config" should {

    lazy val app = salatAppBuilder.configure(Map(
      ("mongodb.default.uri" -> "mongodb://127.0.0.1:27017/salat-test"))).build

    lazy val salat = app.injector.instanceOf[PlaySalat]

    running(app) {
      "start" in {
        salat must beAnInstanceOf[PlaySalat]
      }

      "return a MongoCollection" in {
        val col = salat.collection("salat-collection")
        col must beAnInstanceOf[MongoCollection]
      }

      "populate hosts from URI" in {
        salat must beAnInstanceOf[PlaySalat]
        val source = salat.source("default")
        source.hosts must equalTo(List(new ServerAddress("127.0.0.1", 27017)))
      }

      /*
      // @todo if we need to test username and password we need to use an embedded mongo instance
      "populate username and password from URI" in {
        salat must beAnInstanceOf[SalatPlugin]
        val source = salat.source("default")
        source.user must equalTo(Some("leon"))
        source.password must equalTo(Some("password"))
      }*/
    }
  }

  "Salat Plugin with multiple uri config" should {
    lazy val app = salatAppBuilder.configure(Map(
      ("mongodb.default.uri" -> "mongodb://127.0.0.1:27017,mongodb.org:1337/salat-test"))).build

    lazy val salat = app.injector.instanceOf[PlaySalat]

    running(app) {
      "start" in {
        salat must beAnInstanceOf[PlaySalat]
      }

      "return a MongoCollection" in {
        val col = salat.collection("salat-collection")
        col must beAnInstanceOf[MongoCollection]
      }

      "populate hosts with multiple URIs" in {
        val source = salat.source("default")
        source.hosts must equalTo(List(new ServerAddress("127.0.0.1", 27017), new ServerAddress("mongodb.org", 1337)))
      }
    }
  }

  "Salat Plugin with replicaset config" should {

    lazy val app = salatAppBuilder.configure(Map(
      ("mongodb.default.db" -> "salat-test"),
      ("mongodb.default.replicaset.host1.host" -> "10.0.0.1"),
      ("mongodb.default.replicaset.host2.host" -> "10.0.0.2"),
      ("mongodb.default.replicaset.host2.port" -> "27018"))).build

    lazy val salat = app.injector.instanceOf[PlaySalat]

    running(app) {
      "start" in {
        salat must beAnInstanceOf[PlaySalat]
      }

      "populate hosts from config" in {
        val source = salat.source("default")
        source.hosts must contain(new ServerAddress("10.0.0.1", 27017), new ServerAddress("10.0.0.2", 27018))
      }
    }
  }

  "Salat Plugin with options" should {

    lazy val app = salatAppBuilder.configure(Map(
      ("mongodb.default.db" -> "salat-with-options"),
      ("mongodb.default.options.connectionsPerHost" -> "255"),
      ("mongodb.default.options.threadsAllowedToBlockForConnectionMultiplier" -> "24"))).build

    lazy val salat = app.injector.instanceOf[PlaySalat]

    running(app) {
      "start" in {
        salat must beAnInstanceOf[PlaySalat]
      }

      "return a MongoCollection" in {
        val col = salat.collection("salat-collection")
        col must beAnInstanceOf[MongoCollection]
      }

      "set mongo options" in {
        val col = salat.collection("salat-collection")
        val options = col.db.underlying.getMongo().getMongoOptions()
        options.connectionsPerHost must equalTo(255)
        options.threadsAllowedToBlockForConnectionMultiplier must equalTo(24)
      }

    }
  }

}
