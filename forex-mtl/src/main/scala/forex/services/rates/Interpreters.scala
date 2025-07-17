package forex.services.rates

import cats.effect._
import forex.client.RatesClient
import forex.services.rates.interpreters.OneFrameLive
import forex.domain.Rate
import forex.util.RetryUtil
import org.typelevel.log4cats.Logger

import java.time.Instant

object Interpreters {

  def live[F[_]: Async](
      client: RatesClient[F],
      cache: Ref[F, Map[Rate.Pair, (Rate, Instant)]],
      counter: Ref[F, Int],
      retryUtil: RetryUtil[F]
  )(implicit logger: Logger[F]): Algebra[F] =
    new OneFrameLive[F](client, cache, counter, retryUtil)
}
