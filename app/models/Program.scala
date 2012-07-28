package models

/**
 * Program of an user
 * User: tomykaira
 * Date: 7/28/12
 * Time: 3:20 PM
 */

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import java.io.File

case class Program(user: String, path: String, version: Int)

object Program {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
      get[String]("program.path") ~
      get[Int]("program.version") map {
      case email~path~version => Program(email, path, version)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByUser(user: String): Seq[Program] = {
    DB.withConnection { implicit connection =>
      SQL("select * from program where email = {email}").on(
        'email -> user
      ).as(Program.simple.*)
    }
  }

  def latestVersion(user: String): Int = {
    DB.withConnection { implicit connection =>
      SQL("select max(version) from program where email = {email}").on(
        'email -> user
      ).apply().head[Int]("c")
    }
  }

  /**
   * Create a User.
   */
  def create(user: User, path: String): Program = {
    val newVersion = this.latestVersion(user.email) + 1
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into program values (
            {email}, {path}, {version}
          )
        """
      ).on(
        'email -> user.email,
        'path -> path,
        'version -> newVersion
      ).executeUpdate()

      Program(user.email, path, newVersion)

    }
  }

}
