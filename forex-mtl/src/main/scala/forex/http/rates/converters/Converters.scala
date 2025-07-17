package forex.http.rates.converters

import forex.domain._
import forex.http.rates.protocol.Protocol.GetApiResponse

object Converters {

  private[rates] implicit class GetApiResponseOps(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.pair.from,
        to = rate.pair.to,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }

}
