package services.facebook

import java.net.URLEncoder
import javax.inject._

import play.api.Logger
import play.api.libs.json.JsSuccess
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
  val log = Logger

  import services.config

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

  def getAccessToken(code: String): Option[String] = {
    log.debug(s"redirect_uri: $redirectURI")
    val resp = ws.url(s"$graphURL/$version/oauth/access_token")
                .withQueryString(
                  "client_id" -> appId,
                  "client_secret" -> secret,
                  "redirect_uri" -> redirectURI,
                  "code" -> code,
                  "scope" -> "email,user_birthday,read_stream,publish_stream,offline_access,create_event,rsvp_event"
                ).get()

    val result = resp map { response =>
      log.debug(response.body)
      response.json.validate[Token] match {
        case t: JsSuccess[Token] =>
          val token = t.get
          log.debug(s"short get : ${token.access_token}")
          shortToken(token.access_token)
          Some(token.access_token)
        case _ => None
      } }
    Await.result(result, 10 seconds)
  }

  def getAccessToken(req: Request[AnyContent]): Option[String] = req.getQueryString("code") flatMap { code => getAccessToken(code) }

  def getLongAccessToken(): Option[String] = {
    val resp = ws.url(s"$graphURL/$version/oauth/access_token")
                  .withQueryString(
                    "grant_type" -> "fb_exchange_token",
                    "client_id" -> appId,
                    "client_secret" -> secret,
                    "fb_exchange_token" -> accessToken
                  ).get

    val result = resp map { response =>
      log.debug(response.body)
      response.json.validate[Token] match {
      case t:JsSuccess[Token] =>
        val token = t.get
        log.debug(s"long get: ${token.access_token}")
        longToken(token.access_token)
        Some(token.access_token)
      case _ => None
    }}

    Await.result(result, 10 seconds)
  }

}