package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
import scala.util.Try


case class Model(id: Option[Int], name: String, status:Int, createDate: Date = new Date(System.currentTimeMillis())
                  ) extends HasStatusWithId[Model] {
  def withId(id: Int): Model = this.copy(Some(id))
}

object Models extends StatusCRUD[Model, ModelT] {
  val table = TableQuery[ModelTable]
  val MVNOTable = TableQuery[MvnoModelTable]

  def update(mvno:Boolean, id: Int, m: Model): Try[Int] = database withDynSession {
    val query = for {
      p <- getTable(mvno) if(p.id is id)
    } yield (p.name)
    Try(query.update(m.name))
  }
}

/**
 * Manage model information
 * 제품 모델..
 * @param tag
 */
class ModelT(tag: Tag, tableName:String) extends DBStatusTable[Model](tag, tableName) {
  def id = column[Int]("id", O.PrimaryKey, O.NotNull, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def status = column[Int]("status", O.NotNull)

  def createDate = column[Date]("create_date", O.NotNull)


  def * = (id.?, name, status, createDate) <>(Model.tupled, Model.unapply)
}

class ModelTable(tag: Tag) extends ModelT(tag,"MODEL")
class MvnoModelTable(tag: Tag) extends ModelT(tag,"MVNO_MODEL")