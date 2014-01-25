package models

import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by teddy on 2014. 1. 20..
 */
package object dbmanager {
  lazy val database = Database.forDataSource(play.api.db.DB.getDataSource())
}
