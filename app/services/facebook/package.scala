package services

/**
  * Created by kigi on 6/15/16.
  */
package object facebook {

  val graphURL = "https://graph.facebook.com"

  val tokenPattern = """access_token=([a-zA-Z0-9]+)&expires=([0-9]+)""".r

}
