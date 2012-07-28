package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.User
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

  def index = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.users.index(user))
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
}
