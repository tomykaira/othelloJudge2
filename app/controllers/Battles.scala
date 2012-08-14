package controllers

import play.api.mvc.{Action, Controller}
import views.html
import models.Battle

/**
 * Battle's RESTful controller
 * User: tomykaira
 * Date: 7/28/12
 * Time: 7:12 PM
 */

object Battles extends Controller {
  def commonHeader (battle: Battle): List[String] = List(
    "Black: " + battle.blackMail,
    "Version: " + battle.blackVersion,
    "White: " + battle.whiteMail,
    "Version: " + battle.whiteVersion,
    "Result: " + battle.status)

  def show (id: Long) = Action {
    Battle.findById(id).map { battle =>
      val output = commonHeader(battle) ++
          List("Kifu: " + battle.kifu,
               "Server output: " + battle.serverOutput)
      Ok(output.mkString("\n")).as("text/plain")
    }.getOrElse {
      NotFound("There is no battle with id " + id).as("text/plain")
    }
  }

  def clientBlack (id: Long) = Action {
    Battle.findById(id).map { battle =>
      val output = commonHeader(battle) ++ List("Black output: " + battle.blackOutput)
      Ok(output.mkString("\n")).as("text/plain")
    }.getOrElse {
      NotFound("There is no battle with id " + id).as("text/plain")
    }
  }

  def clientWhite (id: Long) = Action {
    Battle.findById(id).map { battle =>
      val output = commonHeader(battle) ++ List("White output: " + battle.whiteOutput)
      Ok(output.mkString("\n")).as("text/plain")
    }.getOrElse {
      NotFound("There is no battle with id " + id).as("text/plain")
    }
  }
}
