package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}


case class Model(id: Option[Int], name: String, status:Int, createDate: Date = new Date(System.currentTimeMillis())
                  ) extends HasStatusWithId[Model] {
  def withId(id: Int): Model = this.copy(Some(id))
}

object Models extends CRUD[Model, ModelTable] {
  val table = TableQuery[ModelTable]

  def update(id: Int, m: Model): Int = database withDynSession {
    val query = for {
      p <- table if(p.id is id)
    } yield (p.name)
    query.update(m.name)
  }
}

/**
 * Manage model information
 * 제품 모델..
 * @param tag
 */
class ModelTable(tag: Tag) extends DBStatusTable[Model](tag, "MODEL") {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def status = column[Int]("status", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)


  def * = (id.?, name, status, createDate) <>(Model.tupled, Model.unapply)
}
