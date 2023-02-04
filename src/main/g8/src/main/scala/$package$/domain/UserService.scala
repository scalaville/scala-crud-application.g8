package $package$.domain

import cats.~>

trait UserService[F[_]] {

  type GG[_]

  def createUser(username: String, email: String): F[User]

  def getUser(username: String): F[User]

  def gToF: GG ~> F
}
