package users.http

import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import users.services.usermanagement.Algebra
import users.domain._
import org.http4s.circe.CirceEntityCodec._
import io.circe.generic.auto._
import org.typelevel.log4cats.Logger
import users.http.error.ErrorResponseBuilder
import users.services.usermanagement.error.Error

class AdminUserRoutes[F[_]: Concurrent](service: Algebra[F])(implicit L: Logger[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "admin" / "users" =>
      service.all().flatMap(_.fold(mapError, Ok(_)))

    case POST -> Root / "admin" / "users" / userId / "block" =>
      service.block(User.Id(userId)).flatMap(_.fold(mapError, Ok(_)))

    case POST -> Root / "admin" / "users" / userId / "unblock" =>
      service.unblock(User.Id(userId)).flatMap(_.fold(mapError, Ok(_)))

    case DELETE -> Root / "admin" / "users" / userId =>
      service.delete(User.Id(userId)).flatMap(_.fold(mapError, _ => NoContent()))
  }

  private def mapError(error: Error): F[Response[F]] =
    ErrorResponseBuilder.fromError(error)
}
