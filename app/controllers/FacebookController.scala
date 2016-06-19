package controllers

import javax.inject.{Inject, Singleton}

import org.apache.commons.codec.binary.Base64
import play.api.Logger
import play.api.mvc.{Action, Controller}
import services.facebook._
import services.config
import org.openqa.selenium._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

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


  def code(code: String) = Action.async {

    val tt: Future[Option[String]] = for (
      code1 <- fb.getAccessToken(code);
      code2 <- fb.getLongAccessToken()
    ) yield code2

    tt map { opt => opt match {
      case Some(ret) => Ok(ret)
      case None => Ok("token error")
    }
    }

  }

  def page(name: String) = Action.async {
    fb.page(name) map {
      case Right(page) => Ok(views.html.page(page))
      case Left(error) => Ok(error.toString)
    }
  }

  def albums(id: String) = Action.async {
    fb.albums(id) map {
      case Right(albums) => Ok(views.html.albums(albums))
      case Left(error) => Ok(error.toString)
    }
  }

  def albumsNext(url: String) = Action.async {
    fb.paging[Albums](new String(Base64.decodeBase64(url), "UTF-8")) map {
      case Right(albums) => Ok(views.html.albums(albums))
      case Left(error) => Ok(error.toString)
    }
  }

  def photos(id: String) = Action.async {
    fb.photos(id) map {
      case Right(photos) => Ok(views.html.photos(photos))
      case Left(error) => Ok(error.toString)
    }
  }

  def photosNext(url: String) = Action.async {
    fb.paging[Photos](new String(Base64.decodeBase64(url), "UTF-8")) map {
      case Right(photos) => Ok(views.html.photos(photos))
      case Left(error) => Ok(error.toString)
    }
  }

  def photo(id: String) = Action.async {
    fb.photo(id) map {
      case Right(photo) => Ok(views.html.photo(photo))
      case Left(error) => Ok(error.toString)
    }
  }

  def posts(id: String) = Action.async {
    fb.posts(id) map {
      case Right(posts) => Ok(views.html.posts(posts))
      case Left(error) => Ok(error.toString)
    }
  }

  def postsNext(url: String) = Action.async {
    fb.paging[Posts](new String(Base64.decodeBase64(url), "UTF-8")) map {
      case Right(posts) => Ok(views.html.posts(posts))
      case Left(error) => Ok(error.toString)
    }
  }

  def likes(id: String) = Action.async {
    fb.likes(id) map {
      case Right(likes) => Ok(views.html.likes(likes))
      case Left(error) => Ok(error.toString)
    }
  }

  def likesNext(url: String) = Action.async {
    fb.paging[Likes](new String(Base64.decodeBase64(url), "UTF-8")) map {
      case Right(likes) => Ok(views.html.likes(likes))
      case Left(error) => Ok(error.toString)
    }
  }
}
