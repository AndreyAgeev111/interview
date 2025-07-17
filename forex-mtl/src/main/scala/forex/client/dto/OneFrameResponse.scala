package forex.client.dto

import io.circe.Decoder

import java.time.OffsetDateTime
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class OneFrameResponse(
    from: String,
    to: String,
    bid: BigDecimal,
    ask: BigDecimal,
    price: BigDecimal,
    time_stamp: OffsetDateTime
)

object OneFrameResponse {
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val rateDecoder: Decoder[OneFrameResponse] = deriveConfiguredDecoder
}
