package apis

import models.dbmanager.{CRUD, ProductLogTable, ProductLog, ProductLogs}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */

object ProductLogAPI extends API[ProductLog, ProductLogTable, CRUD[ProductLog,ProductLogTable]] {
  val tableManager:CRUD[ProductLog, ProductLogTable] = ProductLogs

  implicit val mappingFormat: Format[ProductLog] = Json.format[ProductLog]
}
