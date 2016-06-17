package controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.mvc.{Action, Controller}
import services.facebook.Facebook
import services.config
import org.openqa.selenium._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class FacebookController @Inject() (fb: Facebook, web: WebDriver) extends Controller {

  val log = Logger("FacebookController")

  web.get("https://www.facebook.com")

  web.findElement(By.id("email")).sendKeys(config.getString("facebook.account"))
  web.findElement(By.id("pass")).sendKeys(config.getString("facebook.password"))
  web.findElement(By.id("pass")).submit()


  def input() = Action {
    Ok(views.html.input())
  }

  def dialog() = Action {
    log.debug("dialog start")
    val redirectURI = config.getString("facebook.redirect-uri")
    val url = fb.loginURL(redirectURI)

    Redirect(url)
  }


  def code(code: String) = Action { implicit request =>

    val longCode = for {
      code <- fb.getAccessToken(request);
      code2 <- fb.getLongAccessToken()
    } yield code2

    Ok(longCode.getOrElse("token error"))
  }

  def page(name: String) = Action.async {
    fb.page(name).map {
      case Right(page) => Ok(page.toString)
      case Left(error) => Ok(error.toString())
    }
  }
}
