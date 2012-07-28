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

  def index = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      Ok(html.users.index(user))
    }.getOrElse(Forbidden)
  }
}
