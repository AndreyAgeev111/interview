package users.http.dto

import derevo.circe.decoder
import derevo.derive
import users.domain._

@derive(decoder)
final case class PasswordUpdateRequest(password: Password)
