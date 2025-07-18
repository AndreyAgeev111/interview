package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.http.rates.converters.Converters.GetApiResponseOps
import forex.http.rates.dto.QueryParams.{FromQueryParam, ToQueryParam}
import forex.programs.RatesProgram
import forex.programs.rates.protocol.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.get(RatesProgramProtocol.GetRatesRequest(from, to))
        .flatMap(Sync[F].fromEither)
        .flatMap { rate =>
          Ok(rate.asGetApiResponse)
        }

    case GET -> Root =>
      BadRequest("Missing required query parameters 'from' and 'to'")
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
