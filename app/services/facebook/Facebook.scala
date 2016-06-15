package services.facebook

import javax.inject.Inject

import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class Facebook @Inject() (ws: WSClient) {

  import services.config

  private var longTermAccessToken = ""

  private var accessToken = ""

  private var redirectURI = ""

  val appId = config.getString("facebook.appid")
  val secret = config.getString("facebook.secret")
  val version = config.getString("facebook.version")
  val simpleAppToken = appId + "|" + secret



  def loginURL(uri: String) = {
    redirectURI = uri
    s"https://www.facebook.com/$version/dialog/oauth?client_id=$appId&redirect_uri=$uri"
  }

  def getAccessToken(code: String): Option[String] = {
    val resp = ws.url(s"$graphURL/$version/oauth/access_token")
                .withQueryString(
                  "grant_type" -> "fb_exchange_token",
                  "client_id" -> appId,
                  "client_secret" -> secret,
                  "fb_exchange_token" -> accessToken
                ).get()

    val result = resp map { response => response.body match {
      case tokenPattern(token, expires) => longTermAccessToken = token; Some(token)
      case _ => None
      }
    }

    Await.result(result, 10 seconds)
  }

  def getAccessToken(req: Request[AnyContent]): Option[String] = ???
}