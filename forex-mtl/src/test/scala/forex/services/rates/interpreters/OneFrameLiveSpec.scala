package forex.services.rates.interpreters

import cats.effect.{ IO, Ref }
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.client.Algebra
import forex.client.error.Errors.Error.OneFrameDecodeFailed
import forex.services.rates.error.Errors.Error.OneFrameLookupFailed
import forex.util.RetryUtilImpl
import forex.util.configuration.RetryConfiguration
import munit.CatsEffectSuite
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant
import scala.concurrent.duration.DurationInt

class OneFrameLiveSpec extends CatsEffectSuite {

  test("OneFrameLive should return cached rate") {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    val rate = Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(123.45), Timestamp.now)
    val now  = Instant.now

    val client: Algebra[IO] = (_: Rate.Pair) => IO.raiseError(new RuntimeException("Should not be called"))

    val cacheRef           = Ref.of[IO, Map[Rate.Pair, (Rate, Instant)]](Map(rate.pair -> (rate, now))).unsafeRunSync()
    val counterRef         = Ref.of[IO, Int](0).unsafeRunSync()
    val retryConfiguration = RetryConfiguration(retryDuration = 100.millis, amount = 3)
    val retryUtil          = new RetryUtilImpl[IO](retryConfiguration)
    val service            = new OneFrameLive[IO](client, cacheRef, counterRef, retryUtil)

    service.get(rate.pair).map {
      case Right(r) => assertEquals(r.price.value.toDouble, 123.45)
      case Left(_)  => fail("Expected cached value")
    }
  }

  test("OneFrameLive should fetch from client and cache the result") {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    val rate = Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(111.11), Timestamp.now)

    var called = false
    val client: Algebra[IO] = (_: Rate.Pair) =>
      IO {
        called = true
        Right(rate)
    }

    val cacheRef           = Ref.of[IO, Map[Rate.Pair, (Rate, Instant)]](Map.empty).unsafeRunSync()
    val counterRef         = Ref.of[IO, Int](0).unsafeRunSync()
    val retryConfiguration = RetryConfiguration(retryDuration = 100.millis, amount = 3)
    val retryUtil          = new RetryUtilImpl[IO](retryConfiguration)
    val service            = new OneFrameLive[IO](client, cacheRef, counterRef, retryUtil)

    for {
      result <- service.get(rate.pair)
      updatedCache <- cacheRef.get
      updatedCount <- counterRef.get
    } yield {
      assertEquals(result, Right(rate))
      assert(called)
      assert(updatedCache.contains(rate.pair))
      assertEquals(updatedCount, 1)
    }
  }

  test("OneFrameLive should fail if request limit exceeded") {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    val pair = Rate.Pair(Currency.EUR, Currency.GBP)

    val counterRef = Ref.of[IO, Int](1001).unsafeRunSync()
    val cacheRef   = Ref.of[IO, Map[Rate.Pair, (Rate, Instant)]](Map.empty).unsafeRunSync()
    val retryUtil  = new RetryUtilImpl[IO](RetryConfiguration(100.millis, 3))

    val client: Algebra[IO] = (_: Rate.Pair) => IO.raiseError(new RuntimeException("Should not be called"))

    val service = new OneFrameLive[IO](client, cacheRef, counterRef, retryUtil)

    service.get(pair).map {
      case Left(OneFrameLookupFailed(msg)) =>
        assert(msg.contains("Daily request limit"), clues(msg))
      case Right(_) =>
        fail("Expected error due to request limit")
    }
  }

  test("OneFrameLive should return mapped error on client failure") {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    val pair       = Rate.Pair(Currency.CHF, Currency.CAD)
    val counterRef = Ref.of[IO, Int](0).unsafeRunSync()
    val cacheRef   = Ref.of[IO, Map[Rate.Pair, (Rate, Instant)]](Map.empty).unsafeRunSync()
    val retryUtil  = new RetryUtilImpl[IO](RetryConfiguration(100.millis, 1))

    val client: Algebra[IO] =
      (_: Rate.Pair) => IO.pure(Left(OneFrameDecodeFailed("bad json")))

    val service = new OneFrameLive[IO](client, cacheRef, counterRef, retryUtil)

    service.get(pair).map {
      case Left(OneFrameLookupFailed(msg)) =>
        assert(msg.contains("Decode failed"))
      case Right(_) =>
        fail("Expected client decode error")
    }
  }
}
