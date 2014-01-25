package apis

import play.api.mvc._
import models.dbmanager.{Products, CRUD, Product, ProductTable}
import play.api.libs.json._
import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */
object ProductAPI extends API[Product,ProductTable,CRUD[Product,ProductTable]] {

  val tableManager: CRUD[Product, ProductTable] = Products
  implicit val mappingFormat: Format[Product] = Json.format[Product]
}
