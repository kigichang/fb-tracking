package controllers

import java.net.URLDecoder
import javax.inject.{Inject, Singleton}

import org.apache.commons.codec.binary.Base64
import org.openqa.selenium._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import services.config
import services.facebook._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class FacebookController @Inject() (fb: Facebook, web: WebDriver) extends Controller {

  val log = Logger("FacebookController")

  /* login facebook user */
  web.get("https://www.facebook.com")
  web.findElement(By.id("email")).sendKeys(config.getString("facebook.account"))
  web.findElement(By.id("pass")).sendKeys(config.getString("facebook.password"))
  web.findElement(By.id("pass")).submit()


  def input(name: Option[String]) = Action {
    name match {
      case Some(n) => Redirect(routes.FacebookController.page(n))
      case None => Ok(views.html.input())
    }

  }

  def dialog() = Action {
    log.debug("dialog start")
    val redirectURI = config.getString("facebook.redirect-uri")
    val url = fb.loginURL(redirectURI)

    Redirect(url)
  }

  /**
    * 取得 long-term user access token
    */
  def code(code: String) = Action.async {

    (for (
      code1 <- fb.getAccessToken(code); /* 取得 user access token */
      code2 <- fb.getLongAccessToken()  /* 轉成 long-term user access token */
    ) yield code2) map { opt => opt match {
      case Some(ret) => Ok(ret)
      case None => Ok("token error")
    }}
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

  def user(id: String) = Action.async {
    fb.user(id) map {
      case Right(user) => Ok(views.html.user(user))
      case Left(error) => Ok(error.toString)
    }
  }

  def debug() = Action.async {
    fb.tokenInfo() map { resp => Ok(resp.json) }
  }

  /**
    * 利用 Chrome Browser 取得個人圖片、 po 文，好友、追隨者清單
    * @param url
    * @return
    */
  def chrome(url: String) = Action {
    val crawl: Try[((String, String), mutable.Buffer[String], mutable.Buffer[(String, String, String)], mutable.Buffer[(String, String, String)])] = Try {

      web.get(URLDecoder.decode(url, "UTF-8"))
      val name = web.findElement(By.cssSelector("img.profilePic")).getAttribute("alt")
      val pic = web.findElement(By.cssSelector("img.profilePic")).getAttribute("src")
      val user = (name, pic)

      val posts = web.findElements(By.cssSelector("div.userContentWrapper")) map { elm => elm.getText }

      if (web.isInstanceOf[JavascriptExecutor]) {
        web.asInstanceOf[JavascriptExecutor].executeScript("window.scrollBy(0,0)", "")
        Thread.sleep(1000)
      }

      web.findElement(By.linkText("Friends")).click()
      Thread.sleep(5 * 1000)

      val friends = web.findElements(By.cssSelector("ul[data-pnref=friends] > li._698")) map { elm =>
        val img = elm.findElement(By.cssSelector("img.img")).getAttribute("src")
        val link = elm.findElement(By.cssSelector("div.fcb > a")).getAttribute("href")
        val name = elm.findElement(By.cssSelector("div.fcb")).getText
        (img, link, name)
      }

      val follows = web.findElements(By.cssSelector("li.fbProfileBrowserListItem")) map { elm =>
        val img = elm.findElement(By.cssSelector("img.img")).getAttribute("src")
        val link = elm.findElement(By.cssSelector("a")).getAttribute("href")
        val name = elm.getText

        (img, link, name)
      }
      (user, posts, friends, follows)
    }

    if(crawl.isSuccess) Ok(views.html.friends(crawl.get._1, crawl.get._2, crawl.get._3, crawl.get._4))
    else Ok(crawl.failed.get.toString)

  }
}
