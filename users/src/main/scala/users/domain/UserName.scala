package users.domain

import cats.kernel.Eq
import io.circe.{Decoder, Encoder}

final case class UserName(value: String) extends AnyVal

object UserName {
  implicit val eq: Eq[UserName] =
    Eq.fromUniversalEquals
  implicit val encoder: Encoder[UserName] = Encoder.encodeString.contramap(_.value)
  implicit val decoder: Decoder[UserName] = Decoder.decodeString.map(UserName(_))
}
