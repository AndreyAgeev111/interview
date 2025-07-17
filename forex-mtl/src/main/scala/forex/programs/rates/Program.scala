package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import forex.domain._
import forex.programs.rates.error.Errors.Error
import forex.programs.rates.error.Errors.toProgramError
import forex.programs.rates.protocol.Protocol
import forex.services.RatesService

class Program[F[_]: Functor](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] =
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value

}

object Program {

  def apply[F[_]: Functor](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
