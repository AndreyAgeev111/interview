package users.main

import cats.data._
import cats.effect.IO
import users.config._
import users.services._

import scala.concurrent.ExecutionContext

object Services {
  val reader: Reader[(ServicesConfig, Executors, Repositories), Services] =
    Reader((Services.apply _).tupled)

  val fromApplicationConfig: Reader[ApplicationConfig, Services] =
    (for {
      config <- ServicesConfig.fromApplicationConfig
      executors <- Executors.fromApplicationConfig
      repositories <- Repositories.fromApplicationConfig
    } yield (config, executors, repositories)) andThen reader
}

final case class Services(config: ServicesConfig, executors: Executors, repositories: Repositories) {
  import executors._
  import repositories._

  implicit val ec: ExecutionContext = serviceExecutor

  val userManagement: UserManagement[IO] =
    UserManagement.unreliable(
      UserManagement.default(userRepository),
      config.users
    )
}
