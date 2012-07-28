package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.{Battle, Program, User}
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
      "email" -> text,
      "name" -> text,
      "password" -> text
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

  def index = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      val battles = Battle.findAll()
      val users = User.findAllWithProgram
      Ok(html.users.index(user, users, battles))
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

  def newBattle(opponentEmail: String) = IsAuthenticated { username => implicit request =>
    def latestOfPrograms(programs: Seq[Program]) = programs.reduceLeft {
      (latest: Program, p) => if (latest.version < p.version) p else latest
    }
    val latestChallengerProgram = latestOfPrograms( Program.findByUser(username) )
    val latestOpponentProgram = latestOfPrograms( Program.findByUser(opponentEmail) )

    Battle.create(latestChallengerProgram, latestOpponentProgram).start()
    Redirect(routes.Users.index).flashing(
      "success" -> "Battle started"
    )
  }
}
