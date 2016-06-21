package services

import play.api.libs.json.Json

/**
  * Created by kigi on 6/20/16.
  */
package object instagram {

  val ApiURL = "https://api.instagram.com"

  case class InstagramError(error: String)

  /* JSON Start*/

  case class Meta(code: Int, error_type: Option[String], error_message: Option[String])
  implicit val metaFormat = Json.format[Meta]

  trait InstagramResult[T] {
    val meta: Meta
    val data: Option[T]
  }


  case class Pagination(next_url: String, next_max_id: String)
  implicit val paginationFormat = Json.format[Pagination]

  case class Counts(media: Int, follows: Int, followed_by: Int)
  implicit val countsFormat = Json.format[Counts]

  case class User(id: String,
                  username: String,
                  full_name: Option[String],
                  first_name: Option[String],
                  last_name: Option[String],
                  profile_picture: String,
                  bio: Option[String],
                  website: Option[String],
                  counts: Option[Counts]
                 )
  implicit val userFormat = Json.format[User]

  case class Token(access_token: String, user: User)
  implicit val tokenFormat = Json.format[Token]


  case class UserResult(override val meta: Meta, override val data: Option[User]) extends InstagramResult[User]
  implicit val userResultFormat = Json.format[UserResult]


  case class SearchResult(override val meta: Meta, override val data: Option[Seq[User]]) extends InstagramResult[Seq[User]]
  implicit val searchResultFormat = Json.format[SearchResult]

  /* JSON End*/

}