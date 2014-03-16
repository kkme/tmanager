package models.dbmanager

/**
 * Created by teddy on 2014. 3. 13..
 */

object MemberLevel {
  object Crud extends Enumeration {
    type CRUD = Value
    val READ = Value(1)
    val WRITE = Value(2)
    val UPDATE = Value(4)
    val DELETE = Value(8)
    val DELETE_FORCE = Value(16)
    val MASTER = Value(255)
  }
  object TableType extends Enumeration {
    type TableType = Value
    val MEMBER = Value(0)
    val MVNO_MEMBER = Value(2)
    val VENDOR = Value(4)
    val MVNO_VENDOR = Value(6)
    val SHOP = Value(8)
    val MVNO_SHOP = Value(10)
    val MODEL = Value(12)
    val MVNO_MODEL = Value(14)
    val MODEL_COLOR = Value(16)
    val MVNO_MODEL_COLOR = Value(18)
    val PRODUCT = Value(20)
    val MVNO_PRODUCT = Value(22)
    val PRODUCT_LOG = Value(24)
    val MVNO_PRODUCT_LOG = Value(26)
    val SALE = Value(28)
    val MVNO_SALE = Value(30)
    val TAKEBACK = Value(32)
    val MVNO_TAKEBACK = Value(34)
  }
}
