package services.instagram

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import services._

import scala.concurrent.Future

/**
  * Created by kigi on 6/20/16.
  */
@Singleton
class Instagram @Inject() (ws: WSClient) {
  val log = Logger("Instagram")
  val appId = config.getString("instagram.appid")
  val secret = config.getString("instagram.secret")
  val version = config.getString("instagram.version")

  private var accessToken = ""
  private var redirectURI = ""

  def token = accessToken

  def login(uri: String) = {
    redirectURI = uri
    s"https://api.instagram.com/oauth/authorize/?client_id=$appId&redirect_uri=$uri&response_type=code&scope=basic+public_content+follower_list+comments+relationships+likes"
  }

  def getAccessToken(code: String) = {
    post(ws, s"$ApiURL/oauth/access_token",
      "client_id" -> appId,
      "client_secret" -> secret,
      "grant_type" -> "authorization_code",
      "redirect_uri" -> redirectURI,
      "code" -> code
    ) map { resp => validate[Token](resp.json) } map {
      case Right(token) =>
        accessToken = token.access_token
        Some(token.access_token)
      case Left(error) => None
    }
  }

  def query[T, A <: InstagramResult[T]](url: String, parameters: (String, String)*)(implicit rds: Reads[A]): Future[Either[String, T]] =
    get(ws, url, (("access_token", token) +: parameters): _*).map { resp =>
      log.debug(resp.body.toString)
      validate[A](resp.json) match {
        case Right(result) =>
          if (result.meta.code == 200) Right(result.data.get)
          else Left(result.meta.toString)
        case Left(error) => Left(error.toString)
      }
    }


  def search(q: String): Future[Either[String, Seq[User]]] =
    query[Seq[User], SearchResult](s"$ApiURL/$version/users/search", "q" -> q, "count" -> "100")

  def user(id: String) = {
    log.debug(s"$ApiURL/$version/users/$id")
    query[User, UserResult](s"$ApiURL/$version/users/$id")
  }
}
