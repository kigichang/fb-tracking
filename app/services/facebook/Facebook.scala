package services.facebook

import javax.inject._

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, Reads}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future

/**
  * Created by kigi on 6/15/16.
  */
@Singleton
class Facebook @Inject() (ws: WSClient) {
  val log = Logger

  import services._

  /**
    * User Access Token longterm 版
    *
    * 通過 User 授權後，取得的 access token 時效比較短，
    * 可以透過 Facebook graph api 取得時效較長的的 token.
    * 時效為 60 天。
    *
    * 切記： User Access Token 不可外流。
    */
  private var longTermAccessToken = ""

  /**
    * User Access Token，通過 User 授權後，可以取得 facebok 給的 User Access Token，
    * 之後要取 Facebook 的資料，都需要用這組 Token。
    *
    * 切記： User Access Token 不可外流。
    */
  private var accessToken = ""

  /**
    * 取得用戶的授權時，告知 Facebook 將結果傳回到我方的那個 URL，
    * 這組 URL 會在 client 端被呼叫。
    *
    * 當用戶授權後，Facebook 會回傳一組 code 給這個 URL，
    * 到時再利用這組 code 去取得 User Access Token。
    *
    */
  private var redirectURI = ""

  /**
    * Facebook App 的 ID，申請時會給
    */
  val appId = config.getString("facebook.appid")

  /**
    * Facebook App 的 secret code，申請時會給，切記不可外洩。
    */
  val secret = config.getString("facebook.secret")

  /**
    * 使用那個 Facebook Graph API 版本
    */
  val version = config.getString("facebook.version")

  /**
    * 自組的 App Access Token, 可以透過 App Access Token 取得 Facebook 上的資料，
    * 但是只有部分。至於可以取得那些資料，就看對方的資料的隱私等級。
    * 要確認的話，可以登出 Facebbook 後，去瀏覽 Facebook，
    * 可以看到的資料，就跟 App Access Token 可取得的資料差不多。
    */
  val simpleAppToken = appId + "|" + secret


  def longToken(token: String) = longTermAccessToken = token

  def shortToken(token: String) = accessToken = token

  def token = if ("" != longTermAccessToken) longTermAccessToken else accessToken

  /**
    * 產生 Faceboook oauth url
    *
    * @param uri 認証完成後，回到那個網址
    * @return
    */
  def loginURL(uri: String) = {
    /* 記錄 redirect uri, 等一下拿 token 時，會用到 */
    redirectURI = uri
    s"https://www.facebook.com/$version/dialog/oauth?client_id=$appId&redirect_uri=$uri"
  }

  /**
    * 取得 User Access Token, 使用 user 授權後，回傳的 code 再去取得 User Access Token。
    *
    * @param code user 授權後，facebook 回傳的一組 code
    */
  def getAccessToken(code: String): Future[Option[String]] = {
    log.debug(s"redirect_uri: $redirectURI")

    val resp: Future[WSResponse] = services.get(ws, s"$GraphURL/$version/oauth/access_token",
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

  /**
    * 使用一般的 User Access Token，轉換成 longterm User Access Token。
    * 如果 longterm 的 token 失效後，不可以由原本的 token 重取一次；
    * 必須從 user 授權的流程開始，取得 user access token 後，再取得 long-term 版。
    */
  def getLongAccessToken(): Future[Option[String]] = {

    val resp = services.get(ws, s"$GraphURL/$version/oauth/access_token",
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

  def query[T](url: String, parameters: (String, String)*)(implicit rds: Reads[T]): Future[Either[JsError, T]] =
    get(ws, url, (("access_token", token) +: parameters): _*) map { response => validate[T](response.json)(rds) }

  /**
    * 取得 facebook 粉絲頁
    */
  def page(id: String): Future[Either[JsError, Page]] = query[Page](s"$GraphURL/$version/$id?$PageFields")

  /**
    * 取得 facebook 用戶或粉絲頁頭像資料
    */
  def picture(id: String): Future[Either[JsError, Picture]] =
    query[Picture](s"$GraphURL/$version/$id/picture", "redirect" -> "false", "type" -> "large")

  /**
    * 取得 facebook 相簿
    */
  def albums(id: String): Future[Either[JsError, Albums]] =
    query[Albums](s"$GraphURL/$version/$id/albums?$AlbumsFields")

  /**
    * Facebook Node 分頁資料
    */
  def paging[T](url: String)(implicit rds: Reads[T]): Future[Either[JsError, T]] = query[T](url)

  /**
    * 取得 facebook 相片
    */
  def photos(id: String): Future[Either[JsError, Photos]] =
    query[Photos](s"$GraphURL/$version/$id/photos?$PhotosFields")

  /**
    * 取得 facebook 單張相片
    */
  def photo(id: String): Future[Either[JsError, Photo]] =
    query[Photo](s"$GraphURL/$version/$id")

  /**
    * 取得 facebook 動態
    */
  def posts(id: String): Future[Either[JsError, Posts]] =
    query[Posts](s"$GraphURL/$version/$id/posts?$PostsFields")

  /**
    * 取得 facebook 按讚
    */
  def likes(id: String): Future[Either[JsError, Likes]] =
    query[Likes](s"$GraphURL/$version/$id/likes?$LikesFields", "summary" -> "true")

  /**
    * 取得 facebook 用戶資料
    */
  def user(id: String): Future[Either[JsError, User]] =
    query[User](s"$GraphURL/$version/$id?$UserFields")

  /**
    * current token information
 *
    * @return
    */
  def tokenInfo(): Future[WSResponse] = {
    get(ws, s"$GraphURL/$version/debug_token", "input_token" -> token, "access_token" -> token)
  }
}