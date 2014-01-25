package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}

/**
 * Created by teddy on 2014. 1. 20..
 */
case class Sale(id: Option[Int], productId: Int, buyerName: String,
                openedPhoneFirst: String, openedPhoneLast: String,
                callingPlan: String, addedService: String, planAmount: Int, serviceAmount: Int, memo: String,
                createDate: Date = new Date(System.currentTimeMillis())
                 ) extends HasOptionId[Sale] {
  def withId(id: Int): Sale = this.copy(Some(id))
}

object Sales extends CRUD[Sale, SaleTable] {
  val table = TableQuery[SaleTable]

  def update(id: Int, m: Sale): Int = ???
}

/**
 * manage the sale table
 * @param tag
 */
class SaleTable(tag: Tag) extends DBTable[Sale](tag, "SALE") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def productId = column[Int]("product_id", O.NotNull)

  def buyerName = column[String]("buyer_name", O.NotNull)

  //앞 부분
  def openedPhoneFirst = column[String]("opened_phone_first", O.NotNull)

  //전화번호 뒷자리
  def openedPhoneLast = column[String]("opened_phone_last", O.NotNull)

  def callingPlan = column[String]("calling_plan", O.NotNull)

  def addedService = column[String]("added_Service", O.NotNull)

  def planAmount = column[Int]("plan_amount", O.NotNull, O.Default(0))

  def serviceAmount = column[Int]("service_amount", O.NotNull, O.Default(0))

  def memo = column[String]("memo", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)

  //foreign keys
  def product = foreignKey("fk_product_sale", productId, Products.table)(_.id)

  //


  def * = (id.?, productId, buyerName, openedPhoneFirst, openedPhoneLast, callingPlan, addedService, planAmount, serviceAmount, memo, createDate) <>(Sale.tupled, Sale.unapply)
}


