package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}

/**
 * Created by teddy on 2014. 1. 20..
 */

case class ProductLog(id:Option[Int], productId:Int, shopId:Int, transferDate:Timestamp = new Timestamp(System.currentTimeMillis())
                       ) extends  HasOptionId[ProductLog] {
  def withId(id: Int): ProductLog = this.copy(Some(id))
}


object ProductLogs extends CRUD[ProductLog, ProductLogTable]{
  val table = TableQuery[ProductLogTable]

  def update(id: Int, m: ProductLog): Int = ???
}

/**
 * Manage the product log table
 * @param tag
 */
class ProductLogTable(tag: Tag) extends DBTable[ProductLog](tag, "PRODUCT_LOG") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def productId = column[Int]("product_id", O.NotNull)

  def shopId = column[Int]("shop_id", O.NotNull)

  def transferDate = column[Timestamp]("transfer_date", O.NotNull)


  //foreign keys
  def product = foreignKey("fk_product_product_log", productId, Products.table)(_.id)

  def shop = foreignKey("fk_shop_product_log", shopId, Shops.table)(_.id)

  //
  def * = (id.?, productId, shopId, transferDate) <> (ProductLog.tupled, ProductLog.unapply)
}


