package forex.services.rates.interpreters

import cats.effect._
import cats.syntax.all._
import forex.domain.Rate
import forex.services.rates.error.Errors.Error
import forex.client.error.Errors.{Error => ClientError}
import forex.services.rates.Algebra
import forex.client.{Algebra => AlgebraClient}
import forex.util.RetryUtil
import org.typelevel.log4cats.Logger
import retry.retryingOnSomeErrors

import java.time.Instant
import scala.concurrent.duration._
import java.time.Duration

class OneFrameLive[F[_]: Async](
    client: AlgebraClient[F],
    cacheRef: Ref[F, Map[Rate.Pair, (Rate, Instant)]],
    counterRef: Ref[F, Int],
    retryUtil: RetryUtil[F],
)(implicit logger: Logger[F])
    extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    for {
      now <- Clock[F].realTimeInstant
      cached <- getFromCache(pair, now)
      result <- cached match {
                 case Some(rate) =>
                   logger.debug(s"Cache hit for $pair") *> rate.asRight[Error].pure[F]
                 case None =>
                   logger.debug(s"Cache miss for $pair") *> fetchAndCache(pair, now)
               }
    } yield result

  private def getFromCache(pair: Rate.Pair, now: Instant): F[Option[Rate]] =
    cacheRef.get.map { cache =>
      cache.get(pair).collect {
        case (rate, timestamp) if isFresh(timestamp, now) => rate
      }
    }

  private def isFresh(timestamp: Instant, now: Instant): Boolean =
    Duration.between(timestamp, now).toMillis < ttl.toMillis

  private def fetchAndCache(pair: Rate.Pair, now: Instant): F[Either[Error, Rate]] =
    for {
      count <- counterRef.get
      result <- if (count >= requestLimit)
                 Error.OneFrameLookupFailed("Daily request limit to OneFrame API exceeded").asLeft[Rate].pure[F]
               else
                 retryingOnSomeErrors[Either[ClientError, Rate]](
                   isWorthRetrying = retryUtil.isWorthToRetry,
                   policy = retryUtil.policy,
                   onError = retryUtil.onError
                 )(client.get(pair).flatTap {
                   case Right(rate) =>
                     logger.debug(s"Fetched and caching rate for $pair") *>
                       updateCacheAndCounter(pair, rate, now)
                   case Left(err) =>
                     logger.error(s"Failed to fetch rate for $pair: $err")
                 }).map(_.leftMap(mapClientError))
    } yield result

  private def updateCacheAndCounter(pair: Rate.Pair, rate: Rate, now: Instant): F[Unit] =
    for {
      _ <- cacheRef.update(_.updated(pair, (rate, now)))
      _ <- counterRef.updateAndGet(_ + 1).flatMap { newCount =>
            logger.debug(s"Cache updated for $pair. Total requests: $newCount")
          }

    } yield ()

  private def mapClientError(error: ClientError): Error = error match {
    case ClientError.OneFrameNotFound(msg)          => Error.OneFrameLookupFailed(s"Not found: $msg")
    case ClientError.OneFrameDecodeFailed(msg)      => Error.OneFrameLookupFailed(s"Decode failed: $msg")
    case ClientError.OneFrameServerError(msg)       => Error.OneFrameLookupFailed(s"Server error: $msg")
    case ClientError.OneFrameUnexpectedStatus(c, m) => Error.OneFrameLookupFailed(s"Unexpected [$c]: $m")
    case ClientError.OneFrameUnauthorized(msg)      => Error.OneFrameLookupFailed(s"Unauthorized: $msg")
  }

  private val ttl: FiniteDuration = 5.minutes
  private val requestLimit        = 1000
}
