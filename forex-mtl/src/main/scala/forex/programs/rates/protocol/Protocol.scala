package forex.programs.rates.protocol

import forex.domain.Currency

object Protocol {

  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )

}
