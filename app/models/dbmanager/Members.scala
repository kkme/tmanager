package models.dbmanager


import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.sql.{Timestamp, Date}


/**
 * Created by teddy on 2014. 1. 20..
 */
case class MemberSignUp(id: String, password: String, passwordConfirmation: String, name: String)
case class MemberLogin(id: String, password: String)
case class Member(id:String, password:String, name:String, level:Short, createDate : Date = new Date(System.currentTimeMillis()))

object Members {
  val member = TableQuery[MemberTable]


  val md = java.security.MessageDigest.getInstance("SHA-256")

  /**
   * Get SHA-256 hashed value of password
   * @param password password
   * @return a SHA-256 hashed password
   */
  def getHashedPassword(password: String) = {
    md.digest(password.getBytes()).map(x => Integer.toHexString(0xFF & x)).mkString
  }

  def exists(id:String) = {
    database withDynSession {
      member.filter(_.id === id).exists.run
    }
  }

  def add(form:MemberSignUp) = database withDynSession {
    val newMember = Member(form.id, getHashedPassword(form.password), form.name, MemberLevel.Visitor.id.toShort)
    member insert newMember
  }

  def validateUserInformation(id:String, password:String) = database withDynSession {
    member.filter { m =>
      (m.id is id) && (m.password is getHashedPassword(password))
    }.exists.run
  }

  def findById(id:String) = database withDynSession {
    val query = for {
      m <- member
      if(m.id is id)
    } yield m
    query.list().headOption
  }


  /**
   * Manage user information
   * @param tag
   */
  class MemberTable(tag: Tag) extends Table[Member](tag, "MEMBER") {
    def id = column[String]("id", O.PrimaryKey, O.NotNull)

    def password = column[String]("password", O.NotNull)

    def name = column[String]("name", O.NotNull)

    def level = column[Short]("level", O.NotNull)

    def createDate = column[Date]("create_date", O.NotNull)

    def * = (id, password, name, level, createDate) <> (Member.tupled, Member.unapply)
  }
}


object MemberLevel extends Enumeration {
  val Master = Value(9:Short)
  val ChiefManager = Value(8:Short)
  val ShopManager = Value(6:Short)
  val Manager = Value(4:Short)
  val Visitor = Value(1:Short)
}