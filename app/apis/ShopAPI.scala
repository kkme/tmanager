package apis

import models.dbmanager.{CRUD, ShopTable, Shop, Shops}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */

object ShopAPI extends API[Shop, ShopTable, CRUD[Shop,ShopTable]] {
  val tableManager:CRUD[Shop, ShopTable] = Shops

  implicit val mappingFormat: Format[Shop] = Json.format[Shop]
}
