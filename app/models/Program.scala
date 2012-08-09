package models

/**
 * Program of an user
 * User: tomykaira
 * Date: 7/28/12
 * Time: 3:20 PM
 */

import play.api.db._
import play.api.Play.current
import play.Logger

import anorm._
import anorm.SqlParser._
import java.io.{InputStreamReader, BufferedReader, File}
import scala.actors._
import scala.actors.Actor._

case class Program(user: String, path: String, version: Int) {
  val COMPILE_TIMEOUT = 20 * 1000
  private val caller = self


  def prepare(): Option[File] = {
    val dir = new File(path.replace(".zip", ""))
    FileUtilities.unzipTo(new File(path), dir)

    val builder = new ProcessBuilder("make")
    builder redirectErrorStream true
    builder directory dir

    val proc = builder.start

    outputReader ! proc

    receiveWithin(COMPILE_TIMEOUT) {
      case TIMEOUT => {
        Logger.warn("Compile of " + this + " is not finished")
        None
      }
      case result: String => {
        val executable = new File(dir, "reversi")
        if (executable.exists)
          Some(executable)
        else {
          Logger.warn("Compile of " + this + " is finished, but reversi does not exist \n" + result)
          None
        }
      }
    }
  }

  override def toString =
    user + "[" + version + "]"

  private val outputReader = actor {
    loop {
      react {
        case proc:Process => {
          val streamReader = new InputStreamReader(proc.getInputStream)
          val bufferedReader = new BufferedReader(streamReader)
          val stringBuilder = new StringBuilder()
          var line:String = null
          while({line = bufferedReader.readLine; line != null}){
            stringBuilder.append(line)
            stringBuilder.append("\n")
          }
          bufferedReader.close
          caller ! stringBuilder.toString
        }
      }
    }
  }
}

object Program {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("program.email") ~
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

  def find(user: String, version: Int): Option[Program] = {
    DB.withConnection { implicit connection =>
      SQL("select * from program where email = {email} and version = {version}").on(
        'email -> user,
        'version -> version
      ).as(Program.simple.singleOpt)
    }
  }

  def latestVersion(user: String): Option[Int] = {
    DB.withConnection { implicit connection =>
      SQL("select max(version) as c from program where email = {email}").on(
        'email -> user
      ).apply().head[Option[Int]]("c")
    }
  }

  /**
   * Create a User.
   */
  def create(user: String): Program = {
    val newVersion = this.latestVersion(user).getOrElse(0) + 1
    val pathSafenName = user.replaceAll("[^a-zA-Z0-9]", "_")
    val path = "/othello-judge/" + pathSafenName + "/" + newVersion + ".zip"
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into program values (
            {email}, {path}, {version}
          )
        """
      ).on(
        'email -> user,
        'path -> path,
        'version -> newVersion
      ).executeUpdate()

      Program(user, path, newVersion)

    }
  }

}
