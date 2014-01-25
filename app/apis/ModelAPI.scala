package apis
import models.dbmanager.{CRUD, ModelTable, Model, Models}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */

object ModelAPI extends API[Model, ModelTable, CRUD[Model,ModelTable]] {
  val tableManager:CRUD[Model, ModelTable] = Models

  implicit val mappingFormat: Format[Model] = Json.format[Model]
}

