package models

/**
 * Matching between user programs
 * User: tomykaira
 * Date: 7/28/12
 * Time: 4:36 PM
 */


import play.api.db._
import play.api.Play.current
import play.Logger

import anorm._
import anorm.SqlParser._


case class Battle(id: Long, blackMail: String, blackVersion: Int,
                   whiteMail: String, whiteVersion: Int, status: BattleStatus,
                   serverOutput: String, blackOutput: String,
                   whiteOutput: String) {
  def shortInfo = {
    "[" + id + "] " + blackMail + " vs. " + whiteMail
  }

  def kifu: String =
    if (status == WhiteWon() || status == BlackWon())
      parseOutputToKifu
    else
      ""

  private def parseOutputToKifu = {
    val kifuLine = new StringBuilder()
    serverOutput.split("\n").foreach { l =>
      if (l.startsWith("Received")) {
        if (l.indexOf("pass") == -1)
          kifuLine.append(l.substring(10, 12))
        else
          kifuLine.append("PS")
      }
    }
    if (kifuLine.endsWith("PSPS"))
      kifuLine.delete(kifuLine.length-4,kifuLine.length)
    kifuLine.toString.toUpperCase
  }
}



object Battle {

  // -- Parsers

  /**
   * Parse a Battle from a ResultSet
   */
  val simple = {
    get[Int]("battle.id") ~
      get[String]("battle.black_mail") ~
      get[Int]("battle.black_version") ~
      get[String]("battle.white_mail") ~
      get[Int]("battle.white_version") ~
      get[String]("battle.status") ~
      get[String]("battle.server_output") ~
      get[String]("battle.black_output") ~
      get[String]("battle.white_output") map {
      case id~cmail~cv~omail~ov~status~server~black~white =>
        Battle(id, cmail, cv, omail, ov, BattleStatus.read(status), server,
        black, white)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByUser(user: String): Seq[Battle] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from battle
          where black_mail = {email} or white_mail = {email}
        """.stripMargin).on(
        'email -> user
      ).as(Battle.simple.*)
    }
  }

  def findAll(): Seq[Battle] = {
    DB.withConnection { implicit connection =>
      SQL(
        "select * from battle".stripMargin).as(Battle.simple.*)
    }
  }

  def findRecent(count: Long): Seq[Battle] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM battle ORDER BY id DESC LIMIT {count}").on('count -> count).as(Battle.simple.*)
    }
  }

  def findById(id: Long): Option[Battle] = {
    DB.withConnection { implicit connection =>
      SQL("select * from battle where id = {id}").on('id -> id).as(Battle.simple.singleOpt)
    }
  }

  /**
   * Create a Battle
   */
  def create(black: Program, white: Program): Battle = {
    DB.withConnection { implicit connection =>

      val id: Long =
        SQL("select next value for battle_seq").as(scalar[Long].single)

      SQL(
        """
          insert into battle
          (id, black_mail, black_version,
          white_mail, white_version, status, server_output, black_output, white_output)
           values (
            {id}, {cmail}, {cv}, {omail}, {ov}, {defaultStatus}, {output}, '', ''
          )
        """
      ).on(
        'id -> id,
        'cmail -> black.user,
        'cv -> black.version,
        'omail -> white.user,
        'ov -> white.version,
        'defaultStatus -> Running().toString,
        'output -> ""
      ).executeUpdate()

      Battle(id, black.user, black.version,
        white.user, white.version, Running(), "", "", "")

    }
  }

  /**
   * Update status
   */
  def update(id:Long, status: BattleStatus, server: String,
    black: String, white: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update battle SET status={status}, server_output = {server},
           black_output = {black}, white_output = {white}
           where id = {id}
        """
      ).on(
        'id -> id,
        'status -> status.toString,
        'server -> server,
        'black -> black,
        'white -> white
      ).executeUpdate()
      ()
    }
  }
}
