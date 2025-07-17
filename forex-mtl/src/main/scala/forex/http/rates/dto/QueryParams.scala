package forex.http.rates.dto

import forex.domain.Currency
import forex.domain.Currency.UNKNOWN
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParamDecoder: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap { str =>
      Currency.fromString(str) match {
        case UNKNOWN  => Left(ParseFailure("Invalid currency", s"Currency '$str' is not supported"))
        case currency => Right(currency)
      }
    }

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

}
