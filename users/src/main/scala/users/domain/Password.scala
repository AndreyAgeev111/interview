package users.domain

import io.circe.{Decoder, Encoder}

final case class Password(value: String) extends AnyVal

object Password {
  implicit val encoder: Encoder[Password] = Encoder.encodeString.contramap(_.value)
  implicit val decoder: Decoder[Password] = Decoder.decodeString.map(Password(_))
}
