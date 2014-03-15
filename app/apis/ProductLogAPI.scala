package apis

import models.dbmanager.{CRUD, ProductLogT, ProductLog, ProductLogs}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by teddy on 2014. 1. 21..
 */

object ProductLogAPI extends API[ProductLog, ProductLogT, CRUD[ProductLog,ProductLogT]] {
  val tableManager:CRUD[ProductLog, ProductLogT] = ProductLogs

  implicit val mappingFormat: Format[ProductLog] = Json.format[ProductLog]

  def list(mvno:Boolean, id:Int)= AuthenticatedAPI.async {
    implicit request => future {
      Ok(Json.toJson(ProductLogs.listByProductId(mvno,id)))
    }
  }
}
