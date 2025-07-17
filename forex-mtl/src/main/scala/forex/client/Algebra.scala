package forex.client

import forex.domain.Rate
import forex.client.error.Errors.Error

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}
