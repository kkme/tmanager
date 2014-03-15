package apis

import models.dbmanager._
import play.api.libs.json._
import java.sql.Date
import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits._
import models.dbmanager.Sale
import scala.util.{Success, Failure}

/**
 * Created by teddy on 2014. 1. 21..
 */

object SaleAPI extends API[Sale, SaleT, CRUD[Sale, SaleT]] {
  val tableManager: CRUD[Sale, SaleT] = Sales

  implicit val mappingFormat: Format[Sale] = Json.format[Sale]

  case class SaleOuterForm(id: Int, productId: Int, buyerName: String, vendorId: Int, modelId: Int, model: String, shopId: Int, colorId: Int, serialNumber: String, status: Int, createDate: Date)

  case class SaleDetailForm(sale: Sale, model: String, modelColor: String, shop: String, vendor: String, serialNumber: String)

  implicit val shopOuterForm = Json.format[SaleOuterForm]
  implicit val shopDetailForm = Json.format[SaleDetailForm]

  def list(mvno: Boolean, start: Option[Long], end: Option[Long]) = AuthenticatedAPI.async {
    future {
      val startDate = start.map(new Date(_))
      val endDate = end.map(new Date(_))
      val result = Sales.list(mvno, startDate, endDate).map(s =>
        SaleOuterForm(s._1, s._2, setName(s._3), s._4, s._5, s._6, s._7, s._8, s._9, s._10, s._11)
      )
      Ok(Json.toJson(result))
    }
  }

  def retrieveItem(mvno: Boolean, id: Int) = AuthenticatedAPI.async {
    future {
      Sales.retrieveById(mvno, id) match {
        case Some(sale) =>
          val newResult = sale.copy(buyerName = setName(sale.buyerName), phoneNumberHead = "", phoneNumberTail = "", ssna = "")
          Ok(Json.toJson(newResult))
        case None => NotFound
      }
    }
  }

  def detail(mvno: Boolean, name: String, phoneLast: String) = AuthenticatedAPI.async {
    future {
      val result = Sales.retrieveDetailByNameAndPhone(mvno, name, phoneLast).map(
        s => SaleDetailForm(s._1, s._2, s._3, s._4, s._5, s._6)
      )
      Ok(Json.toJson(result))
    }
  }

  def takeBack(mvno: Boolean, id: Int) = AuthenticatedAPI.async(parse.json) {
    implicit request => future {
      (request.body \ "reason").validate[String].fold(
        error => BadRequest(error.toString),
        reason => Sales.takeBack(mvno, id, s"반품(${reason})") match {
          case Success(affectedCount: Int) if (affectedCount > 0) => NoContent
          case Success(_) => NotFound
          case Failure(e) => InternalServerError(e.getStackTraceString)
        }
      )
    }
  }

  def setName(name: String) = {
    if (name.length > 1)
      name(0) + "*" + name.substring(2)
    else
      name
  }
}
