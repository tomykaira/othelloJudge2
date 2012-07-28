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

object Users {

  def index = Action {
    Ok(html.users.index("Your new application is ready."))
  }
}
