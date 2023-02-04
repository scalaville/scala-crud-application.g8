package $package$.domain

import sttp.tapir.Schema
case class User(username: String, email: String)

object User {
  implicit val schemaForUser: Schema[User] = Schema.string
}