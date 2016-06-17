package services

import play.api.libs.json._

/**
  * Created by kigi on 6/15/16.
  */
package object facebook {

  val GraphURL = "https://graph.facebook.com"

  //val tokenPattern = """access_token=([a-zA-Z0-9]+)&expires=([0-9]+)""".r


  case class Token(access_token: String, token_type: String, expires_in: Int)

  implicit val tokenFormat = Json.format[Token]

  case class ErrorDetail(message: String, `type`: String, code: Int, fbtrace_id: Option[String])

  implicit val errorDetailFormat = Json.format[ErrorDetail]

  case class Error(error: ErrorDetail)

  implicit val errorFormat = Json.format[Error]

  case class Page(id: String,
                  name: String,
                  about: String,
                  website: String,
                  link: String,
                  fan_count: Int,
                  personal_info: Option[String],
                  affiliation: Option[String]
                 )

  implicit val pageFormat: OFormat[Page] = Json.format[Page]

  val PageFields = s"?fields=about,fan_count,name,website,affiliation,link,personal_info"

  case class Picture(url: String)

  implicit val pictureFormat = Json.format[Picture]

}
