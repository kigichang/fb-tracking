package services

import play.api.libs.json.Json

/**
  * Created by kigi on 6/20/16.
  */
package object instagram {

  val ApiURL = "https://api.instagram.com"

  case class InstagramError(error: String)

  /* JSON Start*/

  /**
    * Instragram API 呼叫後的結果資料，如果是成功，則 code = 200 且沒有 errorType 及 errorMessage
    */
  case class Meta(code: Int, error_type: Option[String], error_message: Option[String])
  implicit val metaFormat = Json.format[Meta]

  /**
    * Instagram 回傳結果格式，
    * 如果失敗，則會沒有 data
    */
  trait InstagramResult[T] {
    val meta: Meta
    val data: Option[T]
  }

  /**
    * 分頁
    * @param next_url
    * @param next_max_id
    */
  case class Pagination(next_url: String, next_max_id: String)
  implicit val paginationFormat = Json.format[Pagination]

  /**
    * 帳號統計數字
    */
  case class Counts(media: Int, follows: Int, followed_by: Int)
  implicit val countsFormat = Json.format[Counts]

  /**
    * 用戶資訊
    */
  case class User(id: String,
                  username: String,
                  full_name: Option[String],
                  first_name: Option[String],
                  last_name: Option[String],
                  profile_picture: String,
                  bio: Option[String],
                  website: Option[String],
                  counts: Option[Counts])
  implicit val userFormat = Json.format[User]

  /**
    * access token 格式
    */
  case class Token(access_token: String, user: User)
  implicit val tokenFormat = Json.format[Token]

  /**
    * 查詢單一 User 回傳格式
    */
  case class UserResult(override val meta: Meta, override val data: Option[User]) extends InstagramResult[User]
  implicit val userResultFormat = Json.format[UserResult]

  /**
    * 搜尋帳號回傳格式
    */
  case class SearchResult(override val meta: Meta, override val data: Option[Seq[User]]) extends InstagramResult[Seq[User]]
  implicit val searchResultFormat = Json.format[SearchResult]

  /* JSON End*/

}
