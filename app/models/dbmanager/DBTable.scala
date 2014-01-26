package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by teddy on 2014. 1. 24..
 */
abstract class DBTable[A](_tableTag: Tag, _schemaName: Option[String], _tableName: String) extends Table[A](_tableTag, _schemaName, _tableName) {
  def this(_tableTag: Tag, _tableName: String) = this(_tableTag, None, _tableName)
  def id:Column[Int]
}
abstract class DBStatusTable[A](_tableTag: Tag, _schemaName: Option[String], _tableName: String) extends  DBTable[A](_tableTag, _schemaName, _tableName) {
  def this(_tableTag: Tag, _tableName: String) = this(_tableTag, None, _tableName)
  def id:Column[Int]
  def status:Column[Int]
}
