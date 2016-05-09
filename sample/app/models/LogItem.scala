package models

import java.util.Date
import com.mongodb.casbah.Imports._

case class LogItem(
  id: ObjectId = new ObjectId,
  datetime: Date = new Date(),
  remoteIP: String,
  remoteAgent: String,
  message: String)
