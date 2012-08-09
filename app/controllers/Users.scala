package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models._
import views._
/**
 * Users controller
 * User: tomykaira
 * Date: 7/28/12
 * Time: 2:20 PM
 */

object Users extends Controller with Secured {

  val registerForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying ("Invalid user information", result => result match {
      case (email, name, password) =>
        try {
          User.create(User(email, name, password))
          true
        } catch {
          case e: Exception => false
        }
    })
  )

  val battleForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "blackTimes" -> number,
      "whiteTimes" -> number
    ) verifying ("Invalid request", result => result match {
      case (email, blackTimes, whiteTimes) =>
        User.findByEmail(email).isDefined
    })
  )

  def index = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      val battles = Battle.findAll()
      val users = User.findAllWithProgram
      Ok(html.users.index(battleForm, user, users, battles))
    }.getOrElse(Forbidden)
  }

  def newAccount = Action { implicit request =>
    Ok(html.users.newAccount(registerForm))
  }

  def create = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.users.newAccount(formWithErrors)),
      user => Redirect(routes.Application.login).flashing(
        "success" -> "Your account is created"
      )
    )
  }

  def upload = IsAuthenticated { username => implicit request =>
    request.body.asMultipartFormData.flatMap(_.file("program")).map { file =>
      import java.io.File
      val program = Program.create(username)
      file.ref.moveTo(new File(program.path))
      Redirect(routes.Users.index).flashing(
        "success" -> "New program is uploaded"
      )
    }.getOrElse {
      Redirect(routes.Users.index).flashing(
        "error" -> "Missing file"
      )
    }
  }

  def newBattle = IsAuthenticated { username => implicit request =>

    battleForm.bindFromRequest.fold ({
      formWithErrors => Redirect(routes.Users.index).flashing(
        "error" -> formWithErrors.errors.map(_.message).mkString
      )}, { (result) => {
        def latestOfPrograms(programs: Seq[Program]) = programs.reduceLeft {
          (latest: Program, p) => if (latest.version < p.version) p else latest
        }

        val (opponentEmail, blackTimes, whiteTimes) = result

        try {
          val latestChallengerProgram = latestOfPrograms( Program.findByUser(username) )
          val latestOpponentProgram = latestOfPrograms( Program.findByUser(opponentEmail) )

          import List._

          // TODO from here
          fill(blackTimes) {
            BattleWorker ! Battle.create(latestChallengerProgram, latestOpponentProgram)
          }

          Redirect(routes.Users.index).flashing(
            "success" -> "Battle started"
          )
        } catch {
          // from programs.reduceLeft
          case e: UnsupportedOperationException =>
            Redirect(routes.Users.index).flashing(
              "error" -> "You have no program yet.  Please upload first."
            )
        }
      }
    })
  }
}
