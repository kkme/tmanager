package models.dbmanager

/**
 * Created by teddy on 2014. 1. 20..
 */

trait HasOptionId[E] {
  this:E =>
  val id:Option[Int]

  def withId(id:Int):E
}

trait HasStatusWithId[E] extends HasOptionId[E] {
  this:E =>

  val id:Option[Int]
  val status:Int

  def withId(id:Int):E
}

