package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}
import scala.util.Try
import scala.slick.driver.MySQLDriver

case class ModelColor(id: Option[Int], modelId: Int, color: String) extends HasOptionId[ModelColor] {
  def withId(id: Int): ModelColor = this.copy(Some(id))
}

object ModelColors extends CRUD[ModelColor, ModelColorT] {
  val table = TableQuery[ModelColorTable]
  val MVNOTable = TableQuery[MvnoModelColorTable]


  def update(mvno:Boolean, id: Int, m: ModelColor) = database withDynSession {
    Try(getTable(mvno).where(_.id is id).update(m))
  }

  def searchByModel(mvno:Boolean, modelId:Int) = database withDynSession {
    getTable(mvno).filter(_.modelId is modelId).list()
  }
}

/**
 * Manage the model's colors
 * model의 Rebate를 설정하거나 할때는 색상과는 아무 연관이 없다.
 * @param tag
 */
class ModelColorT(tag: Tag, modelName:String) extends DBTable[ModelColor](tag, modelName) {
  def id = column[Int]("id", O.NotNull, O.PrimaryKey, O.AutoInc)

  def modelId = column[Int]("model_id", O.NotNull)

  def color = column[String]("color", O.NotNull)

  def model = foreignKey("fk_model_model_color", modelId, Models.table)(_.id)

  def * = (id.?, modelId, color) <>(ModelColor.tupled, ModelColor.unapply)
}

class MvnoModelColorTable(tag:Tag) extends ModelColorT(tag, "MVNO_MODEL_COLOR")
class ModelColorTable(tag:Tag) extends ModelColorT(tag, "MODEL_COLOR")
