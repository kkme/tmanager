package apis

import models.dbmanager._
import play.api.libs.json._
import java.sql.{Timestamp, Date}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.future
import scala.util.{Failure, Success}
import models.dbmanager.Product
import play.api.Logger
import java.util.Calendar

/**
 * Created by teddy on 2014. 1. 21..
 */
case class ProductOuterForm(id: Int, vendor: String, shop: String, model: String, color: String, serialNumber: String, status: Int, createDate: Date)

case class TakeBackListForm(id: Int, vendor: String, shop: String, model: String, color: String, serialNumber: String, status: Int, createDate: Timestamp, memo: String)

object ProductAPI extends API[Product, ProductT, CRUD[Product, ProductT]] {


  val tableManager: CRUD[Product, ProductT] = Products
  implicit val mappingFormat: Format[Product] = Json.format[Product]
  implicit val productOuterFormat: Format[ProductOuterForm] = Json.format[ProductOuterForm]
  implicit val takeBackListForm: Format[TakeBackListForm] = Json.format[TakeBackListForm]

  def list(mvno: Boolean, status: Option[Int], startDate: Option[Long]) = AuthenticatedAPI.async {
    implicit request =>
      future {
        val date =startDate map { mill=>
            val now = new Date(System.currentTimeMillis())
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(now.getTime)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            new Date(cal.getTimeInMillis)
        }

        date match {
          case Some(sDate) =>
            val result = Products.list2(mvno, status, Some(sDate), None)
            Ok(Json.toJson(result))
          case None =>
            val result = Products.list(mvno, status.getOrElse(1), None, None).map(i => ProductOuterForm(i._1, i._2, i._3, i._4, i._5, i._6, i._7, i._8))
            Ok(Json.toJson(result))
        }

        //        getCacheOrSet(className + s"list:${isMvno}:${status}",
        //          Ok(Json.toJson(result))
        //        )
      }
  }

  def traceList(mvno: Boolean, serialNumber: String, modelId: Option[Int], vendorId: Option[Int]) =  AuthenticatedAPI.async{
    implicit request => future {
      val result = Products.traceList(mvno, modelId, serialNumber, vendorId).map(i => ProductOuterForm(i._1, i._2, i._3, i._4, i._5, i._6, i._7, i._8))
      Ok(Json.toJson(result))
    }
  }

  def takeBackList(mvno: Boolean, start: Option[Long], end: Option[Long]) = AuthenticatedAction.async {
    implicit request =>
      future {
        val startDate = start.map(new Timestamp(_))
        val endDate = end.map(new Timestamp(_))
        val result = Products.takeBackList(mvno, startDate, endDate).map(i => TakeBackListForm(i._1, i._2, i._3, i._4, i._5, i._6, i._7, i._8, i._9))
        Ok(Json.toJson(result))
      }
  }

  case class SeveralMoveForm(shopId: Int, products: List[Int])

  implicit val severalMoveForm = Json.format[SeveralMoveForm]

  def moveList(mvno: Boolean) = AuthenticatedAPI.async(parse.json) {
    implicit request => future {
      request.body.validate[SeveralMoveForm].fold(
        error => BadRequest(error.toString),
        form => Products.moveShops(mvno, form.shopId, form.products) match {
          case Success(affectedCount: Int) if (affectedCount > 0) => NoContent
          case Success(_) => NotFound
          case Failure(e) => InternalServerError(e.getStackTraceString)
        }
      )
    }
  }


  def takeBack(mvno: Boolean, id: Int) = AuthenticatedAPI.async(parse.json) {
    implicit request => future {
      (request.body \ "reason").validate[String].fold(
        error => BadRequest(error.toString),
        reason => Products.takeBack(mvno, id, s"반품(${reason})") match {
          case Success(affectedCount: Int) if (affectedCount > 0) => NoContent
          case Success(_) => NotFound
          case Failure(e) => InternalServerError(e.getStackTraceString)
        }
      )
    }
  }

  def changeBackToReady(mvno: Boolean, id: Int) = AuthenticatedAPI.async {
    implicit request => future {
      Products.changeTakeBackToReady(mvno, id) match {
        case Success(affectedCount: Int) if (affectedCount > 0) => NoContent
        case Success(_) => NotFound
        case Failure(e) => InternalServerError(e.getStackTraceString)
      }
    }
  }

}
