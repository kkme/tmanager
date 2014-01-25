package apis

import models.dbmanager.{Vendors, CRUD, Vendor, VendorTable}
import play.api.libs.json.{Json, Format}

/**
 * Created by teddy on 2014. 1. 25..
 */
object VendorAPI extends API[Vendor, VendorTable, CRUD[Vendor,VendorTable]] {
  val tableManager: CRUD[Vendor, VendorTable] = Vendors
  implicit val mappingFormat: Format[Vendor] = Json.format[Vendor]
}
