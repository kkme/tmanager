package apis

import models.dbmanager.{Vendors, CRUD, Vendor, VendorT}
import play.api.libs.json.{Json, Format}

/**
 * Created by teddy on 2014. 1. 25..
 */
object VendorAPI extends API[Vendor, VendorT, CRUD[Vendor,VendorT]] {
  val tableManager: CRUD[Vendor, VendorT] = Vendors
  implicit val mappingFormat: Format[Vendor] = Json.format[Vendor]
}
