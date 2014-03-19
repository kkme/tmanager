package apis
import models.dbmanager.{CRUD, ModelT, Model, Models}
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import java.sql.Date
import java.text.SimpleDateFormat
import play.api.Play.current
import scala.concurrent.Future
import scala.util.{Failure, Success}
import play.api.cache.Cache
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by teddy on 2014. 1. 21..
 */

object ModelAPI extends API[Model, ModelT, CRUD[Model,ModelT]] {
  val tableManager:CRUD[Model, ModelT] = Models

  implicit val mappingFormat: Format[Model] = Json.format[Model]

  case class NewModelForm(name:String, colors:List[String])

  implicit val newModelFormFormat = Json.format[NewModelForm]

  override def add(mvno:Boolean) = AuthenticatedAPI.async(parse.json) {
    implicit request =>
      Future {
        addAttribute(request.body, Json.obj(
          "createDate" -> JsString(""),
          "modifiedDate" -> JsString(""),
          "status" -> JsNumber(models.dbmanager.Status.READY.id)
        )).validate[NewModelForm].fold(
            invalid = (error) => BadRequest(JsError.toFlatJson(error)),
            valid = (form) =>
              Models.createWithColors(mvno, form.name, form.colors) match {
                case Success(id) =>
                  Cache.remove(className + mvno.toString + "list")
                  Ok(Json.toJson(Model(Some(id),form.name, models.dbmanager.Status.READY.id)))
                case Failure(e:MySQLIntegrityConstraintViolationException) => Forbidden("중복된 데이터가 존재하여 추가할 수 없습니다.")
                case Failure(e) =>
                  InternalServerError(e.getStackTraceString)
              })
      }
  }
}

