package $package$.httpApi

import cats.effect.Async
import cats.implicits._
import io.micrometer.prometheus.PrometheusMeterRegistry
import $package$.domain._
import $package$.http.BaseHttp
import log.effect.LogWriter
import org.http4s.HttpRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{EndpointInput, Tapir}

class HttpApi[F[_]: Async](service: UserService[F], metrics: PrometheusMeterRegistry)(implicit log: LogWriter[F])
  extends BaseHttp[F]
    with Tapir {

  import HttpApi._

  private val interpreter = Http4sServerInterpreter[F]()

  private val apiContextPath = List("api", "v1")

  private val createUserEndpoint =
    baseEndpoint
      .in("users")
      .post
      .in(jsonBody[CreateUserRequest])
      .out(jsonBody[CreateUserResponse])
      .serverLogic { request =>
        service
          .createUser(request.username, request.email)
          .map(CreateUserResponse)
          .toOut
      }

  private val getUserEndpoint =
    baseEndpoint
      .in(("users" / path[String]("username")).mapTo[GetUserRequest])
      .get
      .out(jsonBody[GetUserResponse])
      .serverLogic { request =>
        service
          .getUser(request.username)
          .map(GetUserResponse)
          .toOut
      }

  private val metricsEndpoint =
    baseEndpoint
      .in("metrics")
      .get
      .out(jsonBody[String])
      .serverLogic { _ =>
        metrics.scrape().pure[F].toOut
      }

  lazy val mainEndpoints = List(createUserEndpoint, getUserEndpoint, metricsEndpoint).map(se =>
    se.prependSecurityIn(apiContextPath.foldLeft(emptyInput: EndpointInput[Unit])(_ / _))
  )

  lazy val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
    .fromServerEndpoints(mainEndpoints, "$name$", "1.0")

  lazy val allEndpoints: List[ServerEndpoint[Any, F]] = mainEndpoints ++ docsEndpoints

  def routes: HttpRoutes[F] = interpreter.toRoutes(allEndpoints)
}

object HttpApi {
  case class CreateUserRequest(username: String, email: String)
  case class CreateUserResponse(user: User)

  case class GetUserRequest(username: String)
  case class GetUserResponse(user: User)
}
