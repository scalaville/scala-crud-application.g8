package $package$.infrastructure

import cats.effect.{Async, Resource}
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import $package$.infrastructure.config.DbConfig
import log.effect.LogWriter
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Database[F[_]: Async](config: DbConfig)(implicit log: LogWriter[F]) {

  def transactor: Resource[F, Transactor[F]] = {

    def buildTransactor(ec: ExecutionContext) = HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.username,
      config.password,
      ec
    )

    ExecutionContexts
      .fixedThreadPool[F](32)
      .flatMap(buildTransactor)
      .evalTap(connectAndMigrate)
  }

  private def connectAndMigrate(xa: Transactor[F]): F[Unit] = {
    (migrate() >> testConnection(xa) >>
      log.info("Database migration & connection test complete")).onError { e =>
      log.warn("Database not available, waiting 5 seconds to retry...", e) >>
        Async[F].sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }
  protected def testConnection(xa: Transactor[F]): F[Int] =
    sql"select 1".query[Int].unique.transact(xa)

  private val flyway = {
    Flyway
      .configure()
      .locations("filesystem:src/main/resources/db/migration")
      .dataSource(config.url, config.username, config.password)
      .load()
  }

  private def migrate(): F[Unit] = {
    if (config.migrateOnStart) {
      Async[F].blocking {
        flyway.migrate()
      }.void
    } else Async[F].unit
  }
}
