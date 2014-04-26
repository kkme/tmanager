package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
import scala.util.Try


case class Product(id: Option[Int], vendorId: Int, modelId: Int, shopId: Int, colorId: Int, serialNumber: String, status: Int,
                   createDate: Date = new Date(System.currentTimeMillis()),
                   modifiedDate: Timestamp = new Timestamp(System.currentTimeMillis())
                    ) extends HasStatusWithId[Product] {
  def withId(id: Int): Product = this.copy(id = Some(id))
}

object Products extends StatusCRUD[Product, ProductT] {
  val table = TableQuery[ProductTable]
  val MVNOTable = TableQuery[MvnoProductTable]


  override def create(mvno: Boolean, s: Product): Try[Int] = database withDynTransaction {
    val shopNameQuery = Shops.getTable(mvno).where(_.id is s.shopId).map(_.name)


    val result = for {
      name <- Try(shopNameQuery.run)
      product <- createQuery(mvno, s)
      unitlog <- ProductLogs.writeLog(mvno, product, name.head + "(으)로 입고")
    } yield product
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }

  def update(mvno: Boolean, id: Int, m: Product) = database withDynSession {
    val query = for {
      p <- getTable(mvno) if (p.id is id)
    } yield (p.colorId, p.serialNumber, p.status)
    Try(query.update(m.colorId, m.serialNumber, m.status))
  }

  //TODO - status는 따로 만들어야 하는거 아닌가???

  def takeBackList(mvno: Boolean, startDate: Option[Timestamp] = None, endDate: Option[Timestamp] = None) = database withDynSession {
    val start = startDate map (v => ((p: ProductLogT) => p.transferDate >= v))
    val end = endDate map (v => ((p: ProductLogT) => p.transferDate <= v))
    val ss = Some(((p: ProductT) => p.status is Status.TAKE_BACK.id))
    val q1 = for {
      (productId, log) <- ProductLogs.getTable(mvno) groupBy (_.productId)
    } yield log.map(_.id).max

    val q2 = (for {
      p <- getTable(mvno) if(p.status is Status.TAKE_BACK.id)
      s <- Shops.getTable(mvno) if (p.shopId is s.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
      c <- ModelColors.getTable(mvno) if (p.colorId is c.id)
      v <- Vendors.getTable(mvno) if (p.vendorId is v.id)
      log <- ProductLogs.withFilters(ProductLogs.getTable(mvno),List(start, end)) if((log.productId is p.id) && (log.id in q1))
    } yield (p.id, v.name, s.name, m.name, c.color, p.serialNumber, p.status, log.transferDate, log.memo))

    q2.list()
  }

  def changeTakeBackToReady(mvno:Boolean, id:Int) = database withDynTransaction {
    val result = for {
      unitLog <- ProductLogs.writeLog(mvno, id, "반품 취소")
      status <- updateStatus(mvno, id, Status.READY)
    } yield status
    if(result.isFailure)
      dynamicSession.rollback()
    result
  }

  def excelList(mvno:Boolean, shopId:Option[Int]) = database withDynSession {
    val shop = shopId map (v => ((p: ProductT) => p.shopId is v))
    val ss = Some(((p: ProductT) => p.status is Status.READY.id))
    val query = for {
      p <- withFilters(getTable(mvno), List(shop,ss))
      s <- Shops.getTable(mvno) if (p.shopId is s.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
      c <- ModelColors.getTable(mvno) if (p.colorId is c.id)
      v <- Vendors.getTable(mvno) if (p.vendorId is v.id)
    } yield {
      (p.id, v.name, s.name, m.name, c.color, p.serialNumber, p.status, p.createDate)
    }
    query.list()

  }

  def list(mvno: Boolean, status: Int, startDate: Option[Date] = None, endDate: Option[Date] = None) = database withDynSession {
    val start = startDate map (v => ((p: ProductT) => p.createDate >= v))
    val end = endDate map (v => ((p: ProductT) => p.createDate <= v))
    val ss = Some(((p: ProductT) => p.status is status))

    val query = for {
      p <- withFilters(getTable(mvno), List(start, end, ss))
      s <- Shops.getTable(mvno) if (p.shopId is s.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
      c <- ModelColors.getTable(mvno) if (p.colorId is c.id)
      v <- Vendors.getTable(mvno) if (p.vendorId is v.id)
    } yield {
      (p.id, v.name, s.name, m.name, c.color, p.serialNumber, p.status, p.createDate)
    }
    query.list()
  }

  def list2(mvno: Boolean, status: Option[Int], startDate: Option[Date] = None, endDate: Option[Date] = None) = database withDynSession {
    val start = startDate map (v => ((p: ProductT) => p.createDate >= v))
    val end = endDate map (v => ((p: ProductT) => p.createDate <= v))
    val ss = status map (v => ((p: ProductT) => p.status is v))

    val query = for {
      p <- withFilters(getTable(mvno), List(start, end, ss))
    } yield {
      p
    }
    query.list()
  }

  def traceList(mvno:Boolean, modelId:Option[Int], serialNumber:String, vendorId:Option[Int]) = database withDynTransaction {
    val model = modelId map (v => ((p: ProductT) => p.modelId is v))
    val vendor = vendorId map (v => ((p: ProductT) => p.vendorId is v))

    val query = for {
      p <- withFilters(getTable(mvno), List(model,vendor)) if (p.serialNumber like  "%"+serialNumber)
      s <- Shops.getTable(mvno) if (p.shopId is s.id)
      m <- Models.getTable(mvno) if (p.modelId is m.id)
      c <- ModelColors.getTable(mvno) if (p.colorId is c.id)
      v <- Vendors.getTable(mvno) if (p.vendorId is v.id)
    } yield {
      (p.id, v.name, s.name, m.name, c.color, p.serialNumber, p.status, p.createDate)
    }
    query.list()
  }

  def moveShops(mvno: Boolean, shopId: Int, products: List[Int]) = database withDynTransaction {
    val shopNameQuery = Shops.getTable(mvno).where(_.id is shopId).map(_.name)

    val result = for {
      name <- Try(shopNameQuery.run)
      productLogs = products.map(pid => ProductLog(None, pid, name.head + "(으)로 이관"))
      updatedProductStatus <- Try(getTable(mvno).where(_.id.inSet(products)).map(_.shopId).update(shopId))
      unitLog <- Try(ProductLogs.getTable(mvno) ++= productLogs)
    } yield {
      updatedProductStatus
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }


  def takeBack(mvno: Boolean, id: Int, reason: String) = database withDynTransaction {
    val result = for {
      unitLog <- ProductLogs.writeLog(mvno, id, reason)
      //혹시 여러명의 사용자가 옮길경우 판매에 존재하는데 재고에서 옮기면 판매 테이블이 삭제되지 않는다.
      deleteSale <- Sales.deleteByProductIdQuery(mvno, id)
      updatedProductStatus <- updateStatus(mvno, id, Status.TAKE_BACK)
    } yield {
      updatedProductStatus
    }
    if (result.isFailure)
      dynamicSession.rollback()
    result
  }
}

/**
 * Manage the product table
 * @param tag
 */
class ProductT(tag: Tag, tableName: String) extends DBStatusTable[Product](tag, tableName) {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def vendorId = column[Int]("vendor_id", O.NotNull)

  def modelId = column[Int]("model_id", O.NotNull)

  def shopId = column[Int]("shop_id", O.NotNull)

  def colorId = column[Int]("color_id", O.NotNull)

  def serialNumber = column[String]("serial_number", O.NotNull)

  def status = column[Int]("status", O.NotNull, O.Default(0: Short))

  def createDate = column[Date]("create_date")

  def modifiedDate = column[Timestamp]("modified_date")

  //foreignKeys
  def vendor = foreignKey("fk_vendor_product", vendorId, Vendors.table)(_.id)

  def model = foreignKey("fk_model_product", modelId, Models.table)(_.id)

  def color = foreignKey("fk_model_color_product", colorId, Models.table)(_.id)

  def shop = foreignKey("fk_shop_product", shopId, Shops.table)(_.id)

  //
  def * = (id.?, vendorId, modelId, shopId, colorId, serialNumber, status, createDate, modifiedDate) <>(Product.tupled, Product.unapply)
}

class ProductTable(tag: Tag) extends ProductT(tag, "PRODUCT")

class MvnoProductTable(tag: Tag) extends ProductT(tag, "MVNO_PRODUCT")
