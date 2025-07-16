package users.http.dto

import derevo.circe.decoder
import derevo.derive
import users.domain._

@derive(decoder)
case class CreateUserRequest(userName: UserName, email: EmailAddress, password: Option[Password])
