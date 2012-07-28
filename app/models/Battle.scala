package models

/**
 * Matching between user programs
 * User: tomykaira
 * Date: 7/28/12
 * Time: 4:36 PM
 */


import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._


case class Battle(id: Long, challengerMail: String, challengerVersion: Int,
                   opponentMail: String, opponentVersion: Int, result: BattleStatus,
                   serverOutput: String)


object Battle {

  // -- Parsers

  /**
   * Parse a Battle from a ResultSet
   */
  val simple = {
    get[Int]("battle.id") ~
      get[String]("battle.challenger_mail") ~
      get[Int]("battle.challenger_version") ~
      get[String]("battle.opponent_mail") ~
      get[Int]("battle.opponent_version") ~
      get[String]("battle.status") ~
      get[String]("battle.output") map {
      case id~cmail~cv~omail~ov~status~output =>
        Battle(id, cmail, cv, omail, ov, BattleStatus.read(status), output)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByUser(user: String): Seq[Program] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from battle
          where challenger_mail = {email} or opponent_mail = {email}
        """.stripMargin).on(
        'email -> user
      ).as(Program.simple.*)
    }
  }

  def findAll(): Seq[Program] = {
    DB.withConnection { implicit connection =>
      SQL(
        "select * from battle".stripMargin).as(Program.simple.*)
    }
  }

  /**
   * Create a Battle
   */
  def create(challenger: Program, opponent: Program): Battle = {
    DB.withConnection { implicit connection =>

      val id: Long =
        SQL("select next value for battle_seq").as(scalar[Long].single)

      SQL(
        """
          insert into battle values (
            {id}, {cmail}, {cv}, {omail}, {ov}, {defaultStatus}, ""
          )
        """
      ).on(
        'id -> id,
        'cmail -> challenger.user,
        'cv -> challenger.version,
        'omail -> opponent.user,
        'defaultStatus -> Running().toString
      ).executeUpdate()

      Battle(id, challenger.user, challenger.version,
        opponent.user, opponent.version, Running(), "")

    }
  }

  /**
   * Update status
   */
  def update(id:Long, status: BattleStatus, output: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update program SET status={status}, output = {output}
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