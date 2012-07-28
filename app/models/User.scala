package models

/**
 * User case class and its manager object copied from ZenTask sample application
 * User: tomykaira
 * Date: 7/28/12
 * Time: 1:56 PM
 */

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User(email: String, name: String, password: String)

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
      get[String]("user.name") ~
      get[String]("user.password") map {
      case email~name~password => User(email, name, password)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(User.simple *)
    }
  }

  /**
   * Retrieve all users with program excluding me.
   */
  def findAllWithProgram: Seq[(User, Program)] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from user INNER JOIN program
          WHERE user.email = program.email
          AND program.version = (SELECT max(version) as mv from program group by email)
        """
      ).as(
        get[String]("user.email") ~
          get[String]("user.name") ~
          get[String]("user.password") ~
          get[String]("program.path") ~
          get[Int]("program.version") map {
          case email~name~password~path~version =>
            (User(email, name, password), Program(email,path,version))
        }*
      )
    }
  }

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {name}, {password}
          )
        """
      ).on(
        'email -> user.email,
        'name -> user.name,
        'password -> user.password
      ).executeUpdate()

      user

    }
  }

}
