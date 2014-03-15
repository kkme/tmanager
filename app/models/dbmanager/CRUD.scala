package models.dbmanager

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.util.Try

/**
 * Created by teddy on 2014. 1. 24..
 */
trait CRUD[A <: HasOptionId[A], T <: DBTable[A]] {
  val table: TableQuery[_ <: T]
  val MVNOTable: TableQuery[_ <: T]

  def getTable(mvno: Boolean) = mvno match {
    case true => MVNOTable
    case false => table
  }

  def create(mvno: Boolean, s: A): Try[Int] = database withDynSession {
    createQuery(mvno,s)
  }
  def createQuery(mvno:Boolean, s:A): Try[Int] = {
    val t = getTable(mvno)
    Try(t.returning(t.map(_.id)) += s)
    //[MySQLIntegrityConstraintViolationException: Duplicate entry '4-1423214' for key 'model_id_serial_number
  }

  def list(mvno: Boolean): List[A] = database withDynSession {
    getTable(mvno).list()
  }

  def update(mvno: Boolean, id: Int, m: A): Try[Int]

  def delete(mvno: Boolean, id: Int):Try[Int] = database withDynSession {
    deleteQuery(mvno,id)
  }

  def deleteQuery(mvno:Boolean, id :Int):Try[Int] =Try(getTable(mvno).where(_.id is id).delete)

  def withFilters[U](table: Query[T, U], list: List[Option[(T) => Column[Boolean]]]): Query[T, U] = list match {
    case Nil => table
    case Some(filter) :: xs => withFilters(table.filter {
      x => filter(x)
    }, xs)
    case None :: xs => withFilters(table, xs)
  }
}


trait StatusCRUD[A <: HasStatusWithId[A], T <: DBStatusTable[A]] extends CRUD[A, T] {

  import models.dbmanager.Status._

  override def list(mvno: Boolean): List[A] = database withDynSession {
    getTable(mvno).filter(_.status is Status.READY.id).list()
  }


  def updateStatus(mvno: Boolean, id: Int, status: STATUS) = Try {
    val query = for {
      target <- getTable(mvno) if (target.id is id)
    } yield (target.status)
    query.update(status.id)
  }
}
