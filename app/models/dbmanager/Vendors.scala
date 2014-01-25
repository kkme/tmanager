package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.Date

/**
 * Created by teddy on 2014. 1. 20..
 */
case class Vendor(id: Option[Int], name: String, telecom: Short, phone: String, isMvno:Boolean,
                  createDate: Date = new Date(System.currentTimeMillis())
                   )extends HasOptionId[Vendor] {
  def withId(id: Int): Vendor = this.copy(Some(id))
}

object Vendors extends CRUD[Vendor, VendorTable]{
  val table = TableQuery[VendorTable]

//  def list(filter: Map[String, String]) = {
//    val nameFilter = filter.get("name") map (v => (i: VendorTable) => i.name like s"%${v}%")
//    val pnFilter = filter.get("phone") map (v => (i: VendorTable) => i.phone like s"%${v}%")
//    val scFilter = filter.get("salecomp") map (v => (i: VendorTable) => i.name like s"%${v}%")
//
//    List(nameFilter, pnFilter, scFilter).filter(_.isDefined).map(_.get) match {
//      case Nil => table
//      case other => table.filter {
//        x =>
//          other.map(filter => filter(x)).reduceLeft(_ && _)
//      }
//    }
//  }
//
//  def count(filter: Map[String, String]) = list(filter).length
//
////  def list(filter: Map[String, String], limit: Int, offset: Int) =
////    list(filter).sortBy(_.name.asc).drop(offset).take(limit)

  def update(id: Int, m: Vendor): Int = database withDynSession {
    val query = for {
      v <- table if(v.id is id)
    } yield (v.name, v.telecom, v.phone, v.isMvno)
    query.update(m.name, m.telecom, m.phone, m.isMvno)
  }


}

/**
 * Manage the vendor table
 * @param tag
 */
class VendorTable(tag: Tag) extends DBTable[Vendor](tag, "VENDOR") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def telecom = column[Short]("telecom", O.NotNull, O.Default(0: Short))

  def phone = column[String]("phone", O.NotNull)

  def isMvno = column[Boolean]("is_mvno", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)

  def * = (id.?, name, telecom, phone, isMvno, createDate) <> (Vendor.tupled, Vendor.unapply)
}

