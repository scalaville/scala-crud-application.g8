package $package$

import cats.effect.{Async, IO, Resource, ResourceApp}
import com.comcast.ip4s.IpLiteralSyntax
import doobie.ConnectionIO
import $package$.httpApi.HttpApi
import $package$.metrics.PrometheusMetrics
import org.http4s.HttpRoutes
import org.http4s.server.Server
import $package$.infrastructure.{ConnectionIOToIO, Database, UserRepository, UserService}
import $package$.infrastructure.config.Config
import log.effect.fs2.SyncLogWriter
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Main extends ResourceApp.Forever {

  implicit val logWriter = SyncLogWriter.consoleLog[IO]

  private def httpServer[F[_]: Async](routes: HttpRoutes[F]): Resource[F, Server] = EmberServerBuilder
    .default[F]
    .withHost(host"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(routes.orNotFound)
    .build

  override def run(args: List[String]): Resource[IO, Unit] = for {
    config <- Resource.eval(IO(ConfigSource.default.loadOrThrow[Config]))
    metrics <- PrometheusMetrics.registry()
    db = new Database[IO](config.db)
    transactor <- db.transactor
    runConnectionIOToIO = ConnectionIOToIO(transactor)
    repository = UserRepository()
    userService = UserService[IO, ConnectionIO](repository, runConnectionIOToIO)
    routes = new HttpApi(userService, metrics).routes
    _ <- httpServer(routes)
  } yield ()
}
