import java.sql.{Timestamp, Date}
import java.text.SimpleDateFormat
import models.dbmanager.{DBTable, HasOptionId, CRUD}
import play.api.libs.json._
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import scala.Some
import scala.Some
import scala.Some

/**
 * Created by teddy on 2014. 1. 21..
 */
package object apis {
  import controllers.Secured
  import play.api.mvc.Controller
  trait API[A <: HasOptionId[A], T <: DBTable[A], C <: CRUD[A, T]] extends Controller with Secured {
    val tableManager:C
    implicit val mappingFormat:Format[A]

    implicit val timeStampFormat= new Format[Timestamp]{
      val format = new SimpleDateFormat("yyyy-MM-dd")
      def reads(json: JsValue): JsResult[Timestamp] = JsSuccess(new Timestamp(System.currentTimeMillis()))

      def writes(o: Timestamp): JsValue = JsString(format.format(o))
    }

    implicit val date= new Format[Date]{
      val format = new SimpleDateFormat("yyyy-MM-dd")
      def reads(json: JsValue): JsResult[Date] = JsSuccess(new Date(System.currentTimeMillis()))

      def writes(o: Date): JsValue = JsString(format.format(o))
    }

    def addAttribute(j:JsValue, key:String, value:JsValue):JsValue = j.as[JsObject] ++ Json.obj(key->value)

    def add = IsAuthenticated {
      (member, request) =>
        request.body.asJson match {
          case Some(json) =>
            val newJson= addAttribute(json,"createDate",JsString(""))
            addAttribute(newJson,"modifiedDate",JsString("")).validate[A].fold(
              invalid = (error) => BadRequest(error.toString),
              valid = (form) =>
                tableManager.create(form) match {
                  case id: Int if (id > 0) => Ok(Json.toJson(form.withId(id)))
                  case _ =>
                    //                Logger.debug("failed to add a C %o", C.unapply())
                    InternalServerError
                })
          case None => BadRequest("Required some json values.")
        }
    }

    def list = IsAuthenticated {
      (member, request)=>
        Ok(Json.toJson(tableManager.list))
    }

    def update(id:Int) = IsAuthenticated {
      (member, request)=>
        request.body.asJson match {
          case Some(json) =>
            json.validate[A].fold(
              invalid = (error) => BadRequest(error.toString),
              valid = (data) =>
                tableManager.update(id, data) match {
                  case affectedCount: Int if (affectedCount > 0) => Accepted
                  case 0 => BadRequest
                  case _ =>
                    //                Logger.debug("failed to add a C %o", C.unapply())
                    InternalServerError
                })
          case None => BadRequest("Required some json values.")
        }
    }

    def delete(id:Int) = IsAuthenticated {
      (member, request)=>
        tableManager.delete(id) match {
          case affectedCount: Int if(affectedCount > 0) => Accepted
          case _ => NotAcceptable
        }
    }
  }
}
