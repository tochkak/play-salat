package ru.tochkak.plugin.salat

import org.bson.types.ObjectId
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.mvc._

object Binders {

  type ObjectId = org.bson.types.ObjectId

  /**
    * QueryString binder for ObjectId
    */
  implicit def objectIdQueryStringBindable = new QueryStringBindable[ObjectId] {
    def bind(key: String, params: Map[String, Seq[String]]) = {
      params.get(key).flatMap(_.headOption).map { value =>
        if (ObjectId.isValid(value))
          Right(new ObjectId(value))
        else
          Left("Cannot parse parameter " + key + " as ObjectId")
      }
    }

    def unbind(key: String, value: ObjectId) = key + "=" + value.toString
  }

  /**
    * Path binder for ObjectId.
    */
  implicit def objectIdPathBindable = new PathBindable[ObjectId] {
    def bind(key: String, value: String): Either[String, ObjectId] = {
      if (ObjectId.isValid(value))
        Right(new ObjectId(value))
      else
        Left("Cannot parse parameter " + key + " as ObjectId")
    }

    def unbind(key: String, value: ObjectId) = value.toString
  }

  /**
    * Convert an ObjectId to a Javascript String
    */
  implicit def objectIdJavascriptLiteral = new JavascriptLiteral[ObjectId] {
    def to(value: ObjectId) = value.toString
  }

  /**
    * Read ObjectId
    */
  implicit object objectIdReads extends Reads[ObjectId] {
    def reads(json: JsValue) = json match {
      case JsString(s) => {
        if (ObjectId.isValid(s))
          JsSuccess(new ObjectId(s))
        else
          JsError(JsonValidationError("validate.error.objectid"))
      }
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("validate.error.expected.jsstring"))))
    }
  }

  /**
    * Write ObjectId
    */
  implicit object objectIdWrites extends Writes[ObjectId] {
    def writes(o: ObjectId) = JsString(o.toString)
  }

}
