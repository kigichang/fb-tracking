package controllers

import javax.inject.{Inject, Singleton}

import org.openqa.selenium.WebDriver
import play.api.mvc.{Action, Controller}
import services.instagram.Instagram
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by kigi on 6/21/16.
  */
@Singleton
class InstagramController @Inject() (ig: Instagram) extends Controller {

  import services._

  def auth() = Action {
    Redirect(ig.login(config.getString("instagram.redirect-uri")))
  }

  def gram(code: String) = Action.async {
    ig.getAccessToken(code) map {
      case Some(token) => Ok(token)
      case None => Ok("token error")
    }
  }
}
