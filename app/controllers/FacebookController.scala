package controllers

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.mvc.{Action, Controller}
import services.facebook.Facebook
import services.config

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class FacebookController @Inject() (fb: Facebook) extends Controller {

  val log = Logger("FacebookController")

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
}
