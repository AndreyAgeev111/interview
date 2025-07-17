package forex.programs.rates

import forex.domain.Rate
import forex.programs.rates.error.Errors.Error
import forex.programs.rates.protocol.Protocol

trait Algebra[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[Error Either Rate]
}
