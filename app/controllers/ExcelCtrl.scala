package controllers

import play.api.mvc.{Action, Controller}

import java.io._
import org.apache.poi.xssf.usermodel._
import play.api.libs.iteratee.Enumerator
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.future
import models.dbmanager.Products
import java.util.Date
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress

/**
 * Created by teddy on 2014. 3. 13..
 */
object ExcelCtrl extends Controller with Secured {
  //    val file = new File("mydata.xlsx")
  //    val outStream = n
  //    val fileOut = new FileOutputStream(file)
  //    val wb = new XSSFWorkbook
  //    val sheet = wb.createSheet("Sheet1")
  //    var rNum = 0
  //    var row = sheet.createRow(rNum)
  //    var cNum = 0
  //    val cell = row.createCell(cNum)
  //    cell.setCellValue("My Cell Value")
  //    wb.write(fileOut)
  //    fileOut.close()
  //    val data = getDataStream
  //    ChunkedResult(
  //      header = ResponseHeader(200),
  //      chunks = dataContent
  //    )
  //    Ok.sendFile( content = file, fileName = _ => "mydata.xlsx")

  def toExcel = Action.async {
    future {
      val wb = new XSSFWorkbook
      val sheet = wb.createSheet("Sheet1")
      for {
        r <- 1 to 1000
        row <- List(sheet.createRow(r))
        c <- 1 to 256
      } yield {
        row.createCell(c).setCellValue(r + c)
      }

      val dataContent: Enumerator[Array[Byte]] = Enumerator.outputStream {
        os =>
          val bos = new BufferedOutputStream(os)
          //          cell.setCellValue("My Cell Value")
          wb.write(bos)
          bos.close()
      }
      Ok.chunked(dataContent).withHeaders(
        "Content-Type" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "Content-Disposition" -> "attachment; filename=result.xlsx"
      )
    }
  }

  def dateFormat(wb: XSSFWorkbook, format: String = "yyyy년MM월dd일 HH시mm분") = {
    val cs = wb.createCellStyle()
    cs.setDataFormat(
      wb.getCreationHelper.createDataFormat().getFormat(format)
    )
    cs
  }

  def productList(mvno: Boolean, shopId: Option[Int]) = AuthenticatedAPI.async {
    future {
      val wb = new XSSFWorkbook()
      val sheet = wb.createSheet("Sheet1")
      val firstRow = sheet.createRow(1)
      firstRow.createCell(1).setCellValue("재고 현황")
      val cell12 = firstRow.createCell(2)
      cell12.setCellValue(new Date(System.currentTimeMillis()))
      cell12.setCellStyle(dateFormat(wb, "yyyy년MM월dd일 HH시mm분"))
      sheet.addMergedRegion(new CellRangeAddress(1,1,2,5))

      val titles = List("#", "입고처", "매장", "모델명", "색상", "일련번호", "생성일")

      val thirdRow = sheet.createRow(3)
      for {
        i <- 0 until titles.length
      } yield thirdRow.createCell(i, Cell.CELL_TYPE_STRING).setCellValue(titles(i))

      val df = dateFormat(wb, "yyyy년MM월dd일")
      var row = 4
      for {
        list <- Products.excelList(mvno, shopId)
      } yield {
        val r = sheet.createRow(row)
        row += 1
        r.createCell(0, Cell.CELL_TYPE_NUMERIC).setCellValue(list._1)
        r.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(list._2)
        r.createCell(2, Cell.CELL_TYPE_STRING).setCellValue(list._3)
        r.createCell(3, Cell.CELL_TYPE_STRING).setCellValue(list._4)
        r.createCell(4, Cell.CELL_TYPE_STRING).setCellValue(list._5)
        r.createCell(5, Cell.CELL_TYPE_STRING).setCellValue(list._6)
        val lastCell = r.createCell(6)
        lastCell.setCellValue(list._8)
        lastCell.setCellStyle(df)
      }
      val dataContent: Enumerator[Array[Byte]] = Enumerator.outputStream {
        os =>
          val bos = new BufferedOutputStream(os)
          //          cell.setCellValue("My Cell Value")
          wb.write(bos)
          bos.close()
      }
      Ok.chunked(dataContent).withHeaders(
        "Content-Type" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "Content-Disposition" -> "attachment; filename=excel.xlsx"
      )
    }
  }
}
