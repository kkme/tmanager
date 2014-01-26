package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}

case class ModelColor(id: Option[Int], modelId: Int, color: String) extends HasOptionId[ModelColor] {
  def withId(id: Int): ModelColor = this.copy(Some(id))
}

object ModelColors extends CRUD[ModelColor, ModelColorTable] {
  val table = TableQuery[ModelColorTable]

  def update(id: Int, m: ModelColor): Int = database withDynSession {
    table.where(_.id is id).update(m)
  }
}

/**
 * Manage the model's colors
 * model의 Rebate를 설정하거나 할때는 색상과는 아무 연관이 없다.
 * @param tag
 */
class ModelColorTable(tag: Tag) extends DBTable[ModelColor](tag, "MODEL_COLOR") {
  def id = column[Int]("id", O.NotNull, O.PrimaryKey, O.AutoInc)

  def modelId = column[Int]("model_id", O.NotNull)

  def color = column[String]("color", O.NotNull)

  def model = foreignKey("fk_model_model_color", modelId, Models.table)(_.id)

  def * = (id.?, modelId, color) <>(ModelColor.tupled, ModelColor.unapply)
}
