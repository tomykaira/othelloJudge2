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
  def show (id: Long) = Action {
    Battle.findById(id).map { battle =>
      val output = List(
        "Black: " + battle.blackMail,
        "Version: " + battle.blackVersion,
        "White: " + battle.whiteMail,
        "Version: " + battle.whiteVersion,
        "Result: " + battle.status,
        "Server output: " + battle.serverOutput,
        "-------------------------------------",
        "Black output: " + battle.blackOutput,
        "-------------------------------------",
        "White output: " + battle.whiteOutput
      )
      Ok(output.mkString("\n")).as("text/plain")
    }.getOrElse {
      NotFound("There is no battle with id " + id).as("text/plain")
    }
  }
}
