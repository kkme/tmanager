package apis

import models.dbmanager._
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat
import scala.concurrent.Future
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import models.dbmanager.ModelColor

/**
 * Created by teddy on 2014. 1. 21..
 */

object ModelColorAPI extends API[ModelColor, ModelColorT, CRUD[ModelColor,ModelColorT]] {
  val tableManager = ModelColors

  implicit val mappingFormat: Format[ModelColor] = Json.format[ModelColor]

  def searchByModel(mvno:Boolean, modelId:Int) = AuthenticatedAPI.async {
    Future {
      getCacheOrSet(modelId + mvno.toString +"colorlist", Ok(Json.toJson(tableManager.searchByModel(mvno, modelId))))
    }
  }

  //원래는 결과에 따라 캐쉬를 지우는게 효율적이지만...
  def updateColor(mvno:Boolean, modelId:Int, id:Int) = {
    Cache.remove(modelId + mvno.toString + "colorlist")
    update(mvno:Boolean, id)
  }

  def create(mvno:Boolean, modelId:Int) = {
    Cache.remove(modelId +mvno.toString + "colorlist")
    add(mvno:Boolean)
  }

  def deleteColor(mvno:Boolean, modelId:Int, id:Int) = {
    Cache.remove(modelId + mvno.toString +"colorlist")
    delete(mvno:Boolean, id)
  }

}
