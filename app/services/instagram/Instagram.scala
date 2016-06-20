package services.instagram

import javax.inject.{Inject, Singleton}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsError
import play.api.libs.ws.WSClient
import services._

import scala.concurrent.Future
/**
  * Created by kigi on 6/20/16.
  */
@Singleton
class Instagram @Inject() (ws: WSClient) {

  val appId = config.getString("instagram.appid")
  val secret = config.getString("instagram.secret")
  val version = config.getString("instagram.version")

  private var accessToken = ""
  private var redirectURI = ""

  def token = accessToken

  def login(uri: String) = {
    redirectURI = uri
    s"https://api.instagram.com/oauth/authorize/?client_id=$appId&redirect_uri=$uri&response_type=code"
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
}
