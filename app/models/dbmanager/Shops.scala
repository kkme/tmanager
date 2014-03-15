package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
import scala.util.Try


case class Shop(id: Option[Int], name: String, phone: String, email: String,
                status:Int, createDate: Date = new Date(System.currentTimeMillis()))
  extends HasStatusWithId[Shop] {
  def withId(id: Int) = this.copy(id=Some(id))
}


object Shops extends StatusCRUD[Shop, ShopT] {
  val table = TableQuery[ShopTable]
  val MVNOTable = TableQuery[MvnoShopTable]


  def update(mvno:Boolean, id:Int, u:Shop) = database withDynSession {
    val query = for {
      s <- getTable(mvno) if(s.id is id)
    } yield (s.name, s.phone, s.email)
    Try(query.update(u.name, u.phone, u.email))
  }
}

class ShopT(tag: Tag, tableName:String) extends DBStatusTable[Shop](tag,tableName) {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def phone = column[String]("phone", O.NotNull)

  def email = column[String]("email", O.NotNull)

  def status = column[Int]("status", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)

  def * = (id.?, name, phone, email, status, createDate) <>(Shop.tupled, Shop.unapply)
}

class ShopTable(tag:Tag) extends ShopT(tag, "SHOP")
class MvnoShopTable(tag:Tag) extends ShopT(tag, "MVNO_SHOP")

