package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException

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
    try {
      table.where(_.id is id).delete
    } catch {
      case e:MySQLIntegrityConstraintViolationException => 0
    }
  }
}


trait StatusCRUD[A <: HasStatusWithId[A], T <: DBStatusTable[A]] extends CRUD[A,T]{
  import models.dbmanager.Status._

  /**
   * When the item has related with some rows, it should update the status. Otherwise, delete it.
   * @param id
   * @return
   */
  override def delete(id: Int): Int = database withDynSession {
    try {
      table.where(_.id is id).delete
    } catch {
      case e:MySQLIntegrityConstraintViolationException => updateStatus(id, Status.DELETED)
    }
  }

  def updateStatus(id:Int, status: STATUS) = database withDynSession {
    val query = for {
      target <- table if(target.id is id)
    } yield (target.status)
    query.update(status.id)
  }
}
