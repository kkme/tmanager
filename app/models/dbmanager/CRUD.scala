package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

/**
 * Created by teddy on 2014. 1. 24..
 */
trait CRUD[A <: HasOptionId[A], T <: DBTable[A]] extends {
  val table:TableQuery[T]

  def create(s: A) = database withDynSession {
    table.returning(table.map(_.id)) += s
  }
  def list:List[A] = database withDynSession {
    table.list()
  }
  def update(id:Int, m:A):Int

  def delete(id:Int) = database withDynSession {
    table.where(_.id is id).delete
  }
}
