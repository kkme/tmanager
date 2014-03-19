import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import java.sql.{Timestamp, Date}
import java.text.SimpleDateFormat
import models.dbmanager._
import play.api.cache.{Cache, Cached}
import play.api.libs.json._
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsSuccess
import play.api.Logger
import play.api.mvc.SimpleResult
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scala.Some
import scala.util.{Failure, Success}

/**
 * Created by teddy on 2014. 1. 21..
 */
package object apis {

  import controllers.Secured
  import play.api.mvc.Controller

  trait API[A <: HasOptionId[A], T <: DBTable[A], C <: CRUD[A, T]] extends Controller with Secured {
    val tableManager: C
    lazy val className = tableManager.getClass.getName
    implicit val mappingFormat: Format[A]

    implicit val timeStampFormat = new Format[Timestamp] {
      val format = new SimpleDateFormat("yyyy-MM-dd")

      def reads(json: JsValue): JsResult[Timestamp] = JsSuccess(new Timestamp(System.currentTimeMillis()))

      def writes(o: Timestamp): JsValue = JsNumber(o.getTime())
    }

    implicit val date = new Format[Date] {
      val format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

      def reads(json: JsValue): JsResult[Date] = JsSuccess(new Date(json.as[Long]))

      def writes(o: Date): JsValue =JsNumber(o.getTime())
        //JsString(format.format(o))
    }

    def addAttribute(j: JsValue, obj: JsObject): JsValue = obj ++ j.as[JsObject]

    def getCacheOrSet(key:String, result : =>SimpleResult) = Cache.getAs[SimpleResult](key) match {
      case Some(result) => result
      case None =>
        Cache.set(key, result)
        result
    }

    def add(mvno:Boolean) = AuthenticatedAPI.async(parse.json) {
      implicit request =>
        Future {
          addAttribute(request.body, Json.obj(
            "createDate" -> JsNumber(System.currentTimeMillis()),
            "modifiedDate" -> JsString(""),
            "status" -> JsNumber(models.dbmanager.Status.READY.id)
          )).validate[A].fold(
              invalid = (error) => BadRequest(JsError.toFlatJson(error)),
              valid = (form) =>
                tableManager.create(mvno, form) match {
                  case Success(id) =>
                    Cache.remove(className + mvno.toString + "list")
                    Ok(Json.toJson(form.withId(id)))
                  case Failure(e:MySQLIntegrityConstraintViolationException) => Forbidden("중복된 데이터가 존재하여 추가할 수 없습니다.")
                  case Failure(e) =>
                    InternalServerError(e.getStackTraceString)
                })
        }
    }

    def list(mvno:Boolean) = AuthenticatedAPI.async {
      implicit request =>
        Future {
          getCacheOrSet(className +mvno.toString+"list", Ok(Json.toJson(tableManager.list(mvno))))
        }
    }

    def update(mvno:Boolean, id: Int) = AuthenticatedAPI.async(parse.json) {
      implicit request => Future {
        request.body.validate[A].fold(
          invalid = (error) => BadRequest(error.toString),
          valid = (data) =>
            tableManager.update(mvno, id, data) match {
              case Success(affectedCount: Int) if (affectedCount > 0) =>
                Cache.remove(className + mvno.toString + "list")
                NoContent
              case Success(_) => NotFound
              case Failure(e:MySQLIntegrityConstraintViolationException) => Forbidden("중복된 데이터가 존재하여 수정이 불가능합니다.")
              case Failure(e) => InternalServerError(e.getStackTraceString)
            })
      }
    }

    def delete(mvno:Boolean, id: Int) = AuthenticatedAPI.async {
      Future {
        tableManager.delete(mvno, id) match {
          case Success(affectedCount: Int) if (affectedCount > 0) =>
            Cache.remove(className + mvno.toString + "list")
            NoContent
          case Success(_) => NotFound
          case Failure(e:MySQLIntegrityConstraintViolationException) => Forbidden("연관된 자료가 존재하여 삭제할 수 없습니다.")
          case Failure(e) => InternalServerError
        }
      }
    }
  }

  trait StatusAPI[A <: HasStatusWithId[A], T <: DBStatusTable[A], C <: StatusCRUD[A, T]]  {
    this: API[A, T, C]  =>

//    def updateStatus(mvno:Boolean, id: Int, status: Int) = AuthenticatedAPI.async {
//      Future {
//        tableManager.updateStatus(mvno, id, models.dbmanager.Status(status)) match {
//          case affectedCount: Int if (affectedCount > 0) =>
//            Cache.remove(className + mvno.toString + "list")
//            NoContent
//          case _ => BadRequest
//        }
//      }
//    }
  }
}
