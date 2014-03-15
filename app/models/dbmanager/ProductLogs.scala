package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
import scala.util.Try

/**
 * Created by teddy on 2014. 1. 20..
 */

case class ProductLog(id:Option[Int], productId:Int, memo:String, transferDate:Timestamp = new Timestamp(System.currentTimeMillis())
                       ) extends  HasOptionId[ProductLog] {
  def withId(id: Int): ProductLog = this.copy(Some(id))
}


object ProductLogs extends CRUD[ProductLog, ProductLogT]{
  val table = TableQuery[ProductLogTable]
  val MVNOTable = TableQuery[MvnoProductLogTable]

  def update(mvno:Boolean, id: Int, m: ProductLog): Try[Int] = ???

  def writeLog(mvno:Boolean, productId:Int, memo:String, transferDate:Timestamp = new Timestamp(System.currentTimeMillis())) = {
    Try(getTable(mvno) += ProductLog(None, productId, memo, transferDate))
  }

  def listByProductId(mvno:Boolean, productId:Int) = database withDynSession {
    val query = for {
      p <- getTable(mvno) if(p.productId is productId)
    } yield p
    query.list()
  }
}

/**
 * Manage the product log table
 * @param tag
 */
class ProductLogT(tag: Tag, tableName:String) extends DBTable[ProductLog](tag, tableName) {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def productId = column[Int]("product_id", O.NotNull)

  def memo = column[String]("memo", O.NotNull)

  def transferDate = column[Timestamp]("transfer_date", O.NotNull)


  //foreign keys
  def product = foreignKey("fk_product_product_log", productId, Products.table)(_.id)


  //
  def * = (id.?, productId, memo, transferDate) <> (ProductLog.tupled, ProductLog.unapply)
}

class ProductLogTable(tag:Tag) extends ProductLogT(tag, "PRODUCT_LOG")
class MvnoProductLogTable(tag:Tag) extends ProductLogT(tag, "MVNO_PRODUCT_LOG")
