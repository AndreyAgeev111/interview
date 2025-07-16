package users

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits.toSemigroupKOps
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import users.config._
import users.http.{ AdminUserRoutes, UserSelfRoutes }
import users.main._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- IO.pure(
                 ApplicationConfig(
                   executors = ExecutorsConfig(
                     services = ExecutorsConfig.ServicesConfig(parallelism = 4)
                   ),
                   services = ServicesConfig(
                     users = ServicesConfig.UsersConfig(failureProbability = 0.1, timeoutProbability = 0.1)
                   )
                 )
               )

      app <- IO(Application.fromApplicationConfig.run(config))

      implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]

      ec              = app.services.executors.serviceExecutor
      userSelfRoutes  = new UserSelfRoutes[IO](app.services.userManagement)
      adminUserRoutes = new AdminUserRoutes[IO](app.services.userManagement)
      httpApp = Router(
        "/" -> (adminUserRoutes.routes <+> userSelfRoutes.routes)
      ).orNotFound

      exitCode <- BlazeServerBuilder[IO]
                   .withExecutionContext(ec)
                   .bindHttp(8080, "0.0.0.0")
                   .withHttpApp(httpApp)
                   .serve
                   .compile
                   .drain
                   .as(ExitCode.Success)

    } yield exitCode
}
