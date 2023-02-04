package $package$.infrastructure

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import $package$.domain.{User, UserRepository}
import cats.implicits._

object UserRepository {
  def apply(): UserRepository[ConnectionIO] = new UserRepository[ConnectionIO] {
    override def createUser(username: String, email: String): ConnectionIO[User] =
      sql"""INSERT INTO USERS (
           |    username,
           |    email
           | ) VALUES (\$username, \$email)
           """.stripMargin.update.run.as(User(username, email))

    override def getUser(username: String): ConnectionIO[Option[User]] =
      sql"""SELECT
           | username,
           | email
           | FROM USERS
           | WHERE username = \$username""".stripMargin
        .query[User]
        .option
  }
}
