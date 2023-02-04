package $package$.infrastructure

import cats.effect.kernel.Sync
import cats.implicits._
import cats.~>
import $package$.common.Fail
import $package$.domain.{User, UserRepository, UserService}

object UserService {

  def apply[F[_], G[_]: Sync](
      userRepository: UserRepository[G],
      liftF: G ~> F
  ): UserService[F] =
    new UserService[F] {

      override type GG[X] = G[X]

      override def getUser(username: String): F[User] =
        gToF(for {
          userOpt <- userRepository.getUser(username)
          user <- userOpt.liftTo[G](
            Fail.NotFound(s"User with username [\$username] not found")
          )
        } yield user)

      override def createUser(username: String, email: String): F[User] =
        gToF(userRepository.createUser(username, email))

      override def gToF: ~>[G, F] = liftF
    }
}
