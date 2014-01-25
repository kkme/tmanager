package apis

import models.dbmanager.{CRUD, SaleTable, Sale, Sales}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */

object SaleAPI extends API[Sale, SaleTable, CRUD[Sale,SaleTable]] {
  val tableManager:CRUD[Sale, SaleTable] = Sales

  implicit val mappingFormat: Format[Sale] = Json.format[Sale]
}
