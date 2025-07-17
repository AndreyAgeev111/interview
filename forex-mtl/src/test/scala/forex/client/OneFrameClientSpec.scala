package forex.client

import cats.effect.IO
import forex.client.configuration.OneFrameClientConfiguration
import forex.client.dto.OneFrameResponse
import forex.domain.{ Currency, Rate }
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.syntax._
import io.circe.generic.auto._
import munit.CatsEffectSuite
import org.typelevel.log4cats.SelfAwareStructuredLogger
import sttp.client4.Response
import sttp.client4.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client4.testing.BackendStub

import java.time.OffsetDateTime
import scala.concurrent.duration.DurationInt

class OneFrameClientSpec extends CatsEffectSuite {

  test("OneFrameClient should parse a valid response") {
    val now      = OffsetDateTime.now()
    val response = List(OneFrameResponse("USD", "JPY", 123.45, 123.45, 123.45, now)).asJson.noSpaces

    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
    implicit val backend: BackendStub[IO] = AsyncHttpClientCatsBackend
      .stub[IO]
      .whenRequestMatchesPartial {
        case req if req.uri.toString().contains("rates") =>
          Response.ok(response)
      }

    val config = OneFrameClientConfiguration("http://localhost", 5.seconds, "test-token")
    val client = new OneFrameClient[IO](config)

    val result = client.get(Rate.Pair(Currency.USD, Currency.JPY))

    result.map(res => assert(res.exists(_.price.value == 123.45)))
  }
}
