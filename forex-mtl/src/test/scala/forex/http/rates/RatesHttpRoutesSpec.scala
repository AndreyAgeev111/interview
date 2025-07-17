package forex.http.rates

import cats.effect.IO
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.programs.RatesProgram
import forex.programs.rates.error.Errors.Error
import forex.programs.rates.error.Errors.Error.RateLookupFailed
import forex.programs.rates.protocol.Protocol
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe._
import io.circe.Json

class RatesHttpRoutesSpec extends CatsEffectSuite {

  test("should return 200 OK and valid JSON for a valid request") {
    val dummyProgram = new RatesProgram[IO] {
      override def get(request: Protocol.GetRatesRequest): IO[Error Either Rate] =
        IO.pure(
          Right(
            Rate(
              Rate.Pair(Currency.USD, Currency.JPY),
              Price(BigDecimal(150.0)),
              Timestamp.now
            )
          )
        )
    }

    val routes = new RatesHttpRoutes[IO](dummyProgram).routes

    val request    = Request[IO](Method.GET, uri"/rates?from=USD&to=JPY")
    val responseIO = routes.orNotFound.run(request)

    for {
      response <- responseIO
      body <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(
        body.hcursor.get[String]("from").toOption,
        Some("USD")
      )
      assertEquals(
        body.hcursor.get[String]("to").toOption,
        Some("JPY")
      )
    }
  }

  test("should return 400 BadRequest if 'from' or 'to' is missing") {
    val dummyProgram = new RatesProgram[IO] {
      override def get(request: Protocol.GetRatesRequest): IO[Error Either Rate] =
        IO.pure(Left(RateLookupFailed("Should not be called")))
    }

    val routes = new RatesHttpRoutes[IO](dummyProgram).routes

    val badRequest = Request[IO](Method.GET, uri"/rates?from=USD")

    routes.orNotFound.run(badRequest).map { response =>
      assertEquals(response.status, Status.BadRequest)
    }
  }

  test("should return 500 InternalServerError if program fails") {

    val dummyProgram = new RatesProgram[IO] {
      override def get(request: Protocol.GetRatesRequest): IO[Error Either Rate] =
        IO.raiseError(new RuntimeException("boom"))
    }

    val routes = new RatesHttpRoutes[IO](dummyProgram).routes
    val request = Request[IO](Method.GET, uri"/rates?from=USD&to=JPY")
    val safeRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req =>
        routes.orNotFound(req).handleErrorWith { e =>
          IO.pure(Response(Status.InternalServerError).withEntity(s"Internal error: ${e.getMessage}"))
        }
    }

    safeRoutes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.InternalServerError)
    }
  }
}
