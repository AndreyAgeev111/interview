package forex.util

import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import forex.util.configuration.RetryConfiguration
import org.typelevel.log4cats.Logger
import retry.RetryDetails.{ GivingUp, WillDelayAndRetry }
import retry.{ RetryDetails, RetryPolicies, RetryPolicy }
import forex.client.error.Errors.Error

trait RetryUtil[F[_]] {
  def onError(error: Error, retryDetails: RetryDetails): F[Unit]
  def policy: RetryPolicy[F]
  def isWorthToRetry(e: Error): F[Boolean]
}

class RetryUtilImpl[F[_]: Async](retryConfiguration: RetryConfiguration)(implicit logger: Logger[F])
    extends RetryUtil[F] {
  import retryConfiguration.{ amount, retryDuration }

  def policy: RetryPolicy[F] =
    RetryPolicies
      .limitRetriesByDelay[F](retryDuration, RetryPolicies.limitRetries(amount))

  def isWorthToRetry(e: Error): F[Boolean] =
    e match {
      case Error.OneFrameServerError(_) | Error.OneFrameUnexpectedStatus(_, _) => true.pure[F]
      case _                                                                   => false.pure[F]
    }

  def onError(error: Error, retryDetails: RetryDetails): F[Unit] = retryDetails match {
    case WillDelayAndRetry(_, retriesSoFar, _) =>
      logger.info(s"Failed to request with $error. So far we have retried $retriesSoFar times.")
    case GivingUp(totalRetries, _) =>
      logger.error(s"Giving up with $error after $totalRetries retries")
  }
}
