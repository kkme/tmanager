package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.Date
import scala.util.Try
import play.api.libs.Crypto

/**
 * Created by teddy on 2014. 1. 20..
 */
case class Sale(id: Option[Int], productId: Int, buyerName: String,
                ssna: String, exTelecom: String,
                phoneNumberHead: String, phoneNumberTail: String, memo: String,
                createDate: Date = new Date(System.currentTimeMillis())
                 ) extends HasOptionId[Sale] {
  def withId(id: Int): Sale = this.copy(Some(id))
}

object Sales extends CRUD[Sale, SaleT] {
  val table = TableQuery[SaleTable]
  val MVNOTable = TableQuery[MvnoSaleTable]

  def update(mvno: Boolean, id: Int, m: Sale): Try[Int] = database.withDynTransaction {
    val query = getTable(mvno).where(_.id is id).map(s => (s.buyerName, s.phoneNumberHead, s.phoneNumberTail, s.ssna, s.exTelecom, s.memo))

    val result = for {
      sale <- Try(query.update(m.buyerName, m.phoneNumberHead, m.phoneNumberTail, m.ssna, m.exTelecom, m.memo))
      unitLog <- ProductLogs.writeLog(mvno, m.productId, m.buyerName + "에게 판매(판매 내용이 수정됨)")
    } yield {
      sale
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }

  override def create(mvno: Boolean, s: Sale): Try[Int] = database.withDynTransaction {
   val result = for {
      product <- Products.updateStatus(mvno, s.productId, Status.SOLD)
      unitlog <- ProductLogs.writeLog(mvno, s.productId, s.buyerName + "에게 판매")
      registerSale <- createQuery(mvno, s)
    } yield {
      registerSale
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }

  def list(mvno: Boolean, startDate: Option[Date] = None, endDate: Option[Date] = None) = database withDynSession {
    val start = startDate map (v => ((s: SaleT) => s.createDate >= v))
    val end = endDate map (v => ((s: SaleT) => s.createDate <= v))

    val query = for {
      s <- withFilters(getTable(mvno), List(start, end))
      p <- Products.getTable(mvno) if (s.productId is p.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
    } yield {
      (s.id, s.productId, s.buyerName, p.vendorId, m.id, m.name, p.shopId, p.colorId, p.serialNumber, p.status, s.createDate)
    }
    query.list()
  }

  def retrieveDetailByNameAndPhone(mvno: Boolean, buyerName: String, phoneLast: String) = database withDynSession {
    val query = for {
      sa <- getTable(mvno) if (sa.buyerName is buyerName) && (sa.phoneNumberTail is phoneLast)
      p <- Products.getTable(mvno) if (sa.productId is p.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
      c <- ModelColors.getTable(mvno) if (p.colorId is c.id)
      s <- Shops.getTable(mvno) if (p.shopId is s.id)
      v <- Vendors.getTable(mvno) if (p.vendorId is v.id)
    } yield {
//      (sa.id, sa.buyerName, sa.ssna, sa.exTelecom, sa.phoneNumberHead, sa.phoneNumberTail,  sa.memo, sa.createDate, m.name, p.serialNumber, c.color, s.name, v.name, p.serialNumber)
      (sa, m.name, c.color, s.name, v.name,  p.serialNumber)
    }
    query.list()
  }

  def retrieveById(mvno: Boolean, id: Int): Option[Sale] = database withDynSession {
    val query = for {
      s <- getTable(mvno) if (s.id is id)
    } yield s
    query.firstOption
  }


  def takeBack(mvno: Boolean, id: Int, reason: String) = database withDynTransaction {
    val productIdQuery = getTable(mvno).filter(_.id is id).map(_.productId)
    val result = for {
      productId <- Try(productIdQuery.run)
      remove <- deleteQuery(mvno, id)
      unitLog <- ProductLogs.writeLog(mvno, productId.head, reason)
      updatedProductStatus <- Products.updateStatus(mvno, productId.head, Status.TAKE_BACK)
    } yield {
      updatedProductStatus
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }

  override def delete(mvno: Boolean, id: Int): Try[Int] = database withDynTransaction {
    val productIdQuery = getTable(mvno).filter(_.id is id).map(_.productId)
    val result = for {
      productId <- Try(productIdQuery.run)
      remove <- deleteQuery(mvno, id)
      unitLog <- ProductLogs.writeLog(mvno, productId.head, "판매 취소")
      updatedProductStatus <- Products.updateStatus(mvno, productId.head, Status.READY)
    } yield {
      remove
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }
}

/**
 * manage the sale table
 * @param tag
 */
class SaleT(tag: Tag, tableName: String) extends DBTable[Sale](tag, tableName) {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def productId = column[Int]("product_id", O.NotNull)

  def buyerName = column[String]("buyer_name", O.NotNull)

  def ssna = column[String]("ssna", O.NotNull)

  def exTelecom = column[String]("ex_telecom", O.NotNull)

  def phoneNumberHead = column[String]("phone_number_head", O.NotNull)

  //전화번호 뒷자리
  def phoneNumberTail = column[String]("phone_number_tail", O.NotNull)

  def memo = column[String]("memo", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)

  //foreign keys
  def product = foreignKey("fk_product_sale", productId, Products.table)(_.id)


  def * = (id.?, productId, buyerName, ssna, exTelecom, phoneNumberHead, phoneNumberTail, memo, createDate) <>(Sale.tupled, Sale.unapply)
}

class SaleTable(tag: Tag) extends SaleT(tag, "SALE")

class MvnoSaleTable(tag: Tag) extends SaleT(tag, "MVNO_SALE")


