package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}


case class Product(id: Option[Int], modelId: Int, shopId: Int, colorId: Int, serialNumber: Int, status: Int,
                   createDate: Date = new Date(System.currentTimeMillis()),
                   modifiedDate: Timestamp = new Timestamp(System.currentTimeMillis())
                    ) extends HasStatusWithId[Product] {
  def withId(id: Int): Product = this.copy(id=Some(id))
}

object Products extends  StatusCRUD[Product, ProductTable] {
  val table = TableQuery[ProductTable]

  def update(id: Int, m: Product): Int = database withDynSession {
    val query = for {
      p <- table if(p.id is id)
    } yield (p.colorId, p.serialNumber, p.status)
    query.update(m.colorId, m.serialNumber, m.status)
  }
  //TODO - status는 따로 만들어야 하는거 아닌가???
}

/**
 * Manage the product table
 * @param tag
 */
class ProductTable(tag: Tag) extends DBStatusTable[Product](tag, "PRODUCT") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def vendorId = column[Int]("vendor_id", O.NotNull)

  def modelId = column[Int]("model_id", O.NotNull)

  def shopId = column[Int]("shop_id", O.NotNull)

  def colorId = column[Int]("color_id", O.NotNull)

  def serialNumber = column[Int]("serial_number", O.NotNull)

  def status = column[Int]("status", O.NotNull, O.Default(0: Short))

  def createDate = column[Date]("create_date")

  def modifiedDate = column[Timestamp]("modified_date")

  //foreignKeys
  def vendor = foreignKey("fk_vendor_product", vendorId, Vendors.table)(_.id)

  def model = foreignKey("fk_model_product", modelId, Models.table)(_.id)

  def color = foreignKey("fk_model_color_product", colorId, Models.table)(_.id)

  def shop = foreignKey("fk_shop_product", shopId, Shops.table)(_.id)

  //
  def * = (id.?, modelId, shopId, colorId, serialNumber, status, createDate, modifiedDate) <>(Product.tupled, Product.unapply)
}


