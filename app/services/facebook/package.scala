package services

import play.api.libs.json._

/**
  * Created by kigi on 6/15/16.
  */
package object facebook {

  val graphURL = "https://graph.facebook.com"

  val tokenPattern = """access_token=([a-zA-Z0-9]+)&expires=([0-9]+)""".r


  case class Token(access_token: String, token_type: String, expires_in: Int)

  implicit val tokenFormat = Json.format[Token]
}
