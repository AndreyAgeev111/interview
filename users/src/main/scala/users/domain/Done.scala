package users.domain

import derevo.circe.decoder
import derevo.derive

@derive(decoder)
case object Done
