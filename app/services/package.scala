import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration.DurationInt

/**
  * Created by kigi on 6/15/16.
  */
package object services {

  val config = ConfigFactory.load

  /**
    * parse json data
    * @param json
    * @param rds
    * @tparam T
    * @return
    */
  def validate[T](json: JsValue)(implicit rds: Reads[T]): Either[JsError, T] = json.validate[T] match {
    case result: JsSuccess[T] => Right(result.get)
    case error: JsError => Left(error)
  }

  /**
    * web service call with GET method
    * @return
    */
  def get(ws: WSClient, url :String, parameters: (String, String)*) = {
    ws.url(url).withFollowRedirects(true)
      .withRequestTimeout(10 seconds)
      .withQueryString(parameters: _*)
      .get
  }

  /**
    * web service call with POST method
    */
  def post(ws: WSClient, url: String, parameters: (String, String)*) =
    ws.url(url)
      .withFollowRedirects(true)
      .withRequestTimeout(10 seconds)
      .post(parameters.groupBy(p => p._1).mapValues(seq => seq.map(v => v._2)))

}
