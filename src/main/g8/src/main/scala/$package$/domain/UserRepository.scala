package $package$.domain

trait UserRepository[G[_]] {
  def createUser(username: String, email: String): G[User]
  def getUser(username: String): G[Option[User]]
}
