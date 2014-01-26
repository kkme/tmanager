package models

import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by teddy on 2014. 1. 20..
 */
package object dbmanager {
  lazy val database = Database.forDataSource(play.api.db.DB.getDataSource())
  object Status extends Enumeration {
    type STATUS = Value
    val DELETED = Value(0)
    val READY = Value(1)
    val SOLD = Value(2)
  }
  object Telecomunication extends Enumeration {
    type TELECOM = Value
    val SKT = Value(1)
    val KT = Value(1)
    val LGT = Value(1)
  }
}

