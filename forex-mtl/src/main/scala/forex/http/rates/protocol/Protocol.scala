package forex.http.rates.protocol

import forex.domain.Currency.show
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val pairEncoder: Encoder[Rate.Pair] = Encoder.instance { pair =>
    Json.obj(
      "from" -> currencyEncoder(pair.from),
      "to" -> currencyEncoder(pair.to)
    )
  }

  implicit val rateEncoder: Encoder[Rate] = Encoder.instance { rate =>
    Json.obj(
      "pair" -> pairEncoder(rate.pair),
      "price" -> Json.fromBigDecimal(rate.price.value),
      "timestamp" -> Json.fromString(rate.timestamp.value.toString)
    )
  }

  implicit val responseEncoder: Encoder[GetApiResponse] = Encoder.instance { res =>
    Json.obj(
      "from" -> currencyEncoder(res.from),
      "to" -> currencyEncoder(res.to),
      "price" -> Json.fromBigDecimal(res.price.value),
      "timestamp" -> Json.fromString(res.timestamp.value.toString)
    )
  }

}
