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
                   serverOutput: String) {
  def shortInfo = {
    "[" + id + "] " + blackMail + " vs. " + whiteMail
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
      get[String]("battle.server_output") map {
      case id~cmail~cv~omail~ov~status~output =>
        Battle(id, cmail, cv, omail, ov, BattleStatus.read(status), output)
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
        white.user, white.version, Running(), "")

    }
  }

  /**
   * Update status
   */
  def update(id:Long, status: BattleStatus, output: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update battle SET status={status}, server_output = {output}
           where id = {id}
        """
      ).on(
        'id -> id,
        'status -> status.toString,
        'output -> output
      ).executeUpdate()
      ()
    }
  }
}
