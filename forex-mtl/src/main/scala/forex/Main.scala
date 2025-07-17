package forex

import cats.effect._
import forex.client.RatesClient
import fs2.Stream
import forex.config._
import forex.domain.Rate
import forex.services.RatesServices
import forex.util.RetryUtilImpl
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client4.Backend
import sttp.client4.asynchttpclient.cats.AsyncHttpClientCatsBackend

import java.time.Instant

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)
}

class Application[F[_]: Async] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      logger <- Stream.eval(Slf4jLogger.create[F])

      implicit0(implicitLogger: SelfAwareStructuredLogger[F]) = logger
      implicit0(backend: Backend[F]) <- Stream.resource(AsyncHttpClientCatsBackend.resource[F]())

      cache <- Stream.eval(Ref.of[F, Map[Rate.Pair, (Rate, Instant)]](Map.empty))
      counter <- Stream.eval(Ref.of[F, Int](0))
      retryUtil    = new RetryUtilImpl[F](config.retryConfiguration)
      ratesClient  = RatesClient[F](config.oneFrameClientConfiguration)
      ratesService = RatesServices.live(ratesClient, cache, counter, retryUtil)

      module = new Module[F](config, ratesService)

      _ <- BlazeServerBuilder[F]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()
}
