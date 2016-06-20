package services.facebook

import javax.inject._

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.DurationInt

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class Facebook @Inject() (ws: WSClient) {
  val log = Logger

  import services._

  private var longTermAccessToken = ""

  private var accessToken = ""

  private var redirectURI = ""

  val appId = config.getString("facebook.appid")
  val secret = config.getString("facebook.secret")
  val version = config.getString("facebook.version")
  val simpleAppToken = appId + "|" + secret


  def longToken(token: String) = longTermAccessToken = token

  def shortToken(token: String) = accessToken = token

  def token = if ("" != longTermAccessToken) longTermAccessToken else accessToken

  def loginURL(uri: String) = {
    redirectURI = uri
    s"https://www.facebook.com/$version/dialog/oauth?client_id=$appId&redirect_uri=$uri"
  }

  def getAccessToken(code: String): Future[Option[String]] = {
    log.debug(s"redirect_uri: $redirectURI")

    val resp: Future[WSResponse] = call(ws, s"$GraphURL/$version/oauth/access_token",
      "client_id" -> appId,
      "client_secret" -> secret,
      "redirect_uri" -> redirectURI,
      "code" -> code,
      "scope" -> "email,user_birthday,read_stream,publish_stream,offline_access,create_event,rsvp_event")

    resp map { response =>
      validate[Token](response.json) match {
        case Right(token) =>
          shortToken(token.access_token)
          Some(token.access_token)
        case Left(error) => None
      }
    }
  }

  //def getAccessToken(req: Request[AnyContent]) = req.getQueryString("code"). flatMap { code => getAccessToken(code) }

  def getLongAccessToken(): Future[Option[String]] = {

    val resp = call(ws, s"$GraphURL/$version/oauth/access_token",
      "grant_type" -> "fb_exchange_token",
      "client_id" -> appId,
      "client_secret" -> secret,
      "fb_exchange_token" -> accessToken)

    resp map { response =>
      validate[Token](response.json) match {
        case Right(token) =>
          longToken(token.access_token)
          Some(token.access_token)
        case Left(error) => None
      }
    }
  }

  def get0(url: String, parameters: (String, String)*): Future[WSResponse] =
    call(ws, url, (("access_token", token) +: parameters): _*)

  def get[T](url: String, parameters: (String, String)*)(implicit rds: Reads[T]): Future[Either[JsError, T]] =
    get0(url, parameters: _*) map { response => validate[T](response.json)(rds) }


  def page(id: String): Future[Either[JsError, Page]] = get[Page](s"$GraphURL/$version/$id?$PageFields")

  def picture(id: String): Future[Either[JsError, Picture]] =
    get[Picture](s"$GraphURL/$version/$id/picture", "redirect" -> "false", "type" -> "large")


  def albums(id: String): Future[Either[JsError, Albums]] =
    get[Albums](s"$GraphURL/$version/$id/albums?$AlbumsFields")

  def paging[T](url: String)(implicit rds: Reads[T]): Future[Either[JsError, T]] = call(ws, url) map { resp => validate[T](resp.json) }

  //def albumsWithURL(url: String): Future[Either[JsError, Albums]] = paging[Albums](url)


  def photos(id: String): Future[Either[JsError, Photos]] =
    get[Photos](s"$GraphURL/$version/$id/photos?$PhotosFields")

  def photo(id: String): Future[Either[JsError, Photo]] =
    get[Photo](s"$GraphURL/$version/$id")

  def posts(id: String): Future[Either[JsError, Posts]] =
    get[Posts](s"$GraphURL/$version/$id/posts?$PostsFields")

  def likes(id: String): Future[Either[JsError, Likes]] =
    get[Likes](s"$GraphURL/$version/$id/likes?$LikesFields", "summary" -> "true")

  def user(id: String): Future[Either[JsError, User]] =
    get[User](s"$GraphURL/$version/$id?$UserFields")

  def tokenInfo(): Future[WSResponse] = {
    call(ws, s"$GraphURL/$version/debug_token", "input_token" -> token, "access_token" -> token)
  }
}