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

  case class Cursor(after: String, before: String)
  implicit val cursorFormat = Json.format[Cursor]

  case class Paging(cursors: Option[Cursor], previous: Option[String], next: Option[String])
  implicit val pagingFormat = Json.format[Paging]

  case class Like(id: String,
                  name: String,
                  link: String,
                  pic: String,
                  profile_type: String)

  implicit val likeFormat = Json.format[Like]

  case class Summary(total_count: Int)
  implicit val summaryFormat = Json.format[Summary]

  case class Likes(data: Seq[Like], paging: Option[Paging], summary: Summary)
  implicit val likesFormat = Json.format[Likes]
  val LikesFields = "fields=id,name,link,pic,pic_large,pic_crop,pic_small,pic_square,profile_type,username"


  case class Cover(id: String,
                   cover_id: String,
                   offset_x: Int,
                   offset_y: Int,
                   source: String)
  implicit val coverFormat = Json.format[Cover]

  case class PictureDetail(url: String,
                           is_silhouette: Boolean,
                           width: Option[Int],
                           height: Option[Int])
  implicit val pictureDetailFormat = Json.format[PictureDetail]

  case class Picture(data: PictureDetail)
  implicit val pictureFormat = Json.format[Picture]

  case class Page(id: String,
                  name: String,
                  cover: Option[Cover],
                  about: Option[String],
                  website: String,
                  link: String,
                  fan_count: Int,
                  picture: Picture,
                  personal_info: Option[String],
                  affiliation: Option[String]
                 )
  implicit val pageFormat: OFormat[Page] = Json.format[Page]
  val PageFields = "fields=about,cover,fan_count,name,website,affiliation,link,personal_info,picture.redirect(false).type(large){height,is_silhouette,url,width}"

  case class From(id: String, name: String)
  implicit val fromFormat = Json.format[From]

  case class CoverPhoto(id: String, created_time: String)
  implicit val coverPhotoFormat = Json.format[CoverPhoto]

  case class Album(can_upload: Boolean,
                   count: Int,
                   cover_photo: Option[CoverPhoto],
                   created_time: String,
                   description: Option[String],
                   from: From,
                   id: String,
                   is_user_facing: Boolean,
                   link: String,
                   name: String,
                   photo_count: Int,
                   picture: Picture,
                   `type`: String,
                   updated_time: String,
                   video_count: Int)
  implicit val albumFormat = Json.format[Album]

  case class Albums(data: Seq[Album], paging: Option[Paging])
  implicit val albumsFormat = Json.format[Albums]
  val AlbumsFields = "fields=can_upload,count,cover_photo,description,created_time,from,id,is_user_facing,link,name,photo_count,type,updated_time,video_count,picture{height,is_silhouette,url,width}"

  case class Image(source: String, width: Int, height: Int)
  implicit val imageFormat = Json.format[Image]

  case class Photo(id: String,
                   created_time: String,
                   height: Int,
                   icon: String,
                   images: Seq[Image],
                   link: String,
                   picture: String,
                   updated_time: String,
                   width: Int,
                   likes: Likes)
  implicit val photoFormat = Json.format[Photo]

  case class Photos(data: Seq[Photo], paging: Option[Paging])
  implicit val photosFormat = Json.format[Photos]

  val PhotosFields = "fields=created_time,height,icon,id,images,link,name,name_tags,picture,updated_time,width,tags,likes.limit(0).summary(true)"

  case class Tag(id: String, name: String, `type`: Option[String], offset: Int, length: Int)
  implicit val tagFormat = Json.format[Tag]

  case class To(id: String, name: String, category: Option[String])
  implicit val toFormat = Json.format[To]

  case class Tos(data: Seq[To], tag: Option[String])
  implicit val tosFormat = Json.format[Tos]

  case class Post(id: String,
                  caption: Option[String],
                  created_time: String,
                  description: Option[String],
                  icon: Option[String],
                  link: Option[String],
                  message: Option[String],
                  message_tags:Option[Seq[Tag]],
                  name: Option[String],
                  object_id: Option[String],
                  picture: Option[String],
                  source: Option[String],
                  status_type: Option[String],
                  story: Option[String],
                  story_tags: Option[Seq[Tag]],
                  `type`: Option[String],
                  updated_time: String,
                  with_tags: Option[Tos],
                  likes: Likes)
  implicit val postFormat = Json.format[Post]

  case class Posts(data: Seq[Post], paging: Option[Paging])
  implicit val postsFormat = Json.format[Posts]

  val PostsFields = s"fields=id,caption,created_time,description,from,icon,link,message,message_tags,name,object_id,picture,properties,source,status_type,story,story_tags,type,updated_time,with_tags,likes.limit(0).summary(true)"



}
