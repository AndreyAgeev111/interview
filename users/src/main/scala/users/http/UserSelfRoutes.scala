package users.http

import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import users.services.usermanagement.Algebra
import users.domain._
import users.http.dto._
import org.http4s.circe.CirceEntityCodec._
import org.typelevel.log4cats.Logger
import users.http.error.ErrorResponseBuilder
import users.services.usermanagement.error.Error

class UserSelfRoutes[F[_]: Concurrent](service: Algebra[F])(implicit L: Logger[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "users" =>
      for {
        body <- req.as[CreateUserRequest]
        result <- service.signUp(body.userName, body.email, body.password)
        resp <- result.fold(mapError, Created(_))
      } yield resp

    case GET -> Root / "users" / userId =>
      service.get(User.Id(userId)).flatMap(_.fold(mapError, Ok(_)))

    case req @ PUT -> Root / "users" / userId / "email" =>
      for {
        body <- req.as[EmailUpdateRequest]
        result <- service.updateEmail(User.Id(userId), body.email)
        resp <- result.fold(mapError, Ok(_))
      } yield resp

    case req @ PUT -> Root / "users" / userId / "password" =>
      for {
        body <- req.as[PasswordUpdateRequest]
        result <- service.updatePassword(User.Id(userId), body.password)
        resp <- result.fold(mapError, Ok(_))
      } yield resp

    case POST -> Root / "users" / userId / "reset-password" =>
      service.resetPassword(User.Id(userId)).flatMap(_.fold(mapError, Ok(_)))
  }

  private def mapError(error: Error): F[Response[F]] =
    ErrorResponseBuilder.fromError(error)
}
