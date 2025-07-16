package users.domain

import io.circe.{Decoder, Encoder}

final case class EmailAddress(value: String) extends AnyVal

object EmailAddress {
  implicit val encoder: Encoder[EmailAddress] = Encoder.encodeString.contramap(_.value)
  implicit val decoder: Decoder[EmailAddress] = Decoder.decodeString.map(EmailAddress(_))
}
