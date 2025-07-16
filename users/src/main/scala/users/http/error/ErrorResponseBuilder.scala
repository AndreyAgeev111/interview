package users.http.error

import cats.Applicative
import cats.syntax.all._
import io.circe.Json
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.{Response, Status}
import org.typelevel.log4cats.Logger
import users.services.usermanagement.error.Error

object ErrorResponseBuilder {

  def fromError[F[_]: Applicative](error: Error)(implicit L: Logger[F]): F[Response[F]] = {
    val (status, code, summary, details) = error match {
      case Error.Exists =>
        (Status.Conflict, "user_exists", "User already exists", "A user with the same username already exists.")
      case Error.NotFound =>
        (Status.NotFound, "user_not_found", "User not found", "The user with the specified ID does not exist.")
      case Error.Blocked =>
        (Status.Forbidden, "user_blocked", "User is blocked", "This user account is currently blocked.")
      case Error.Deleted =>
        (Status.Gone, "user_deleted", "User is deleted", "This user account has been deleted.")
      case Error.Active =>
        (Status.BadRequest, "user_already_active", "User is already active", "The user is already in active state.")
      case Error.System(cause) =>
        (
          Status.InternalServerError,
          "internal_error",
          "System error occurred",
          Option(cause.getMessage).getOrElse("Unknown error")
        )
    }

    val body = Json.obj(
      "errorCode"   -> Json.fromString(code),
      "error"       -> Json.fromString(summary),
      "description" -> Json.fromString(details)
    )

    L.warn(s"[$code] $summary - $details") *>
      Response[F](status).withEntity(body).pure[F]
  }
}