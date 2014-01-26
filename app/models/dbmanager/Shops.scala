package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
//import scala.slick.driver.MySQLDriver


case class Shop(id: Option[Int], name: String, phone: String, email: String,
                isOnlyMvno: Boolean, status:Int, createDate: Date = new Date(System.currentTimeMillis()))
  extends HasStatusWithId[Shop] {
  def withId(id: Int) = this.copy(id=Some(id))
}


object Shops extends StatusCRUD[Shop, ShopTable] {
//  val shop = TableQuery[ShopTable]

  val table = TableQuery[ShopTable]

  def update(id:Int, u:Shop) = database withDynSession {
    val query = for {
      s <- table if(s.id is id)
    } yield (s.name, s.phone, s.email, s.isOnlyMvno)
    query.update(u.name, u.phone, u.email, u.isOnlyMvno)
  }
}

class ShopTable(tag: Tag) extends DBStatusTable[Shop](tag,"SHOP") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def phone = column[String]("phone", O.NotNull)

  def email = column[String]("email", O.NotNull)

  def isOnlyMvno = column[Boolean]("only_mvno", O.NotNull)

  def status = column[Int]("status", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)

  def * = (id.?, name, phone, email, isOnlyMvno, status, createDate) <>(Shop.tupled, Shop.unapply)
}


