package apis

import models.dbmanager.{CRUD, ModelColorTable, ModelColor, ModelColors}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Created by teddy on 2014. 1. 21..
 */

object ModelColorAPI extends API[ModelColor, ModelColorTable, CRUD[ModelColor,ModelColorTable]] {
  val tableManager:CRUD[ModelColor, ModelColorTable] = ModelColors

  implicit val mappingFormat: Format[ModelColor] = Json.format[ModelColor]
}
