package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  private val log = Logger

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    log.debug(s"facebook account ${config.getString("facebook.account")}")
    log.debug(s"facebook password ${config.getString("facebook.password")}")
    log.debug(s"facebook appid ${config.getString("facebook.appid")}")
    log.debug(s"facebook secret ${config.getString("facebook.secret")}")
    Ok(views.html.index("Your new application is ready."))
  }

}
