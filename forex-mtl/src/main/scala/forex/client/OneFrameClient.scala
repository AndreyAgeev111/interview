package forex.client

import cats.effect.Async
import forex.client.dto.OneFrameResponse
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.client.error.Errors.Error
import cats.syntax.all._
import forex.client.configuration.OneFrameClientConfiguration
import forex.domain.Currency.UNKNOWN
import org.typelevel.log4cats.Logger
import sttp.client4._
import sttp.client4.circe._

class OneFrameClient[F[_]: Async](
    oneFrameClientConfiguration: OneFrameClientConfiguration
)(implicit
  backend: Backend[F],
  logger: Logger[F])
    extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val query = uri"${oneFrameClientConfiguration.baseUrl}/rates?pair=${pair.from}${pair.to}"

    val request = basicRequest
      .get(query)
      .header("token", oneFrameClientConfiguration.token)
      .readTimeout(oneFrameClientConfiguration.timeout)
      .response(asJson[List[OneFrameResponse]])

    for {
      _ <- logger.debug(s"Requesting rate for ${pair.from}${pair.to} from OneFrame...")
      response <- backend.send(request)
      result <- response match {
        case Response(Right(data), code, _, _, _, _) if code.isSuccess =>
          data.headOption match {
            case Some(rate) =>
              for {
                fromCurrency <- Currency.fromString(rate.from) match {
                                 case UNKNOWN  => logger.warn(s"Unknown from currency: ${rate.from}") *> UNKNOWN.pure[F]
                                 case currency => currency.pure[F]
                               }

                toCurrency <- Currency.fromString(rate.to) match {
                               case UNKNOWN  => logger.warn(s"Unknown to currency: ${rate.to}") *> UNKNOWN.pure[F]
                               case currency => currency.pure[F]
                             }
              } yield
                Right(
                  Rate(
                    Rate.Pair(fromCurrency, toCurrency),
                    Price(rate.price),
                    Timestamp(rate.time_stamp)
                  )
                )

            case None =>
              logger.error("Empty response from OneFrame") *>
                Left(Error.OneFrameDecodeFailed("Empty response from OneFrame")).pure[F]
          }

        case Response(Left(deserializationError), _, _, _, _, _) =>
          logger.error(s"Deserialization failed: $deserializationError") *>
            Left(Error.OneFrameDecodeFailed(deserializationError.toString)).pure[F]

        case Response(_, code, _, _, _, _) if code.code == 401 =>
          logger.warn("Unauthorized request to OneFrame (401)") *>
            Left(Error.OneFrameUnauthorized("Invalid or missing token")).pure[F]

        case Response(_, code, _, _, _, _) if code.code == 404 =>
          logger.warn(s"Rate.Pair ${pair.from}${pair.to} not found (404)") *>
            Left(Error.OneFrameNotFound(s"Rate.Pair ${pair.from}${pair.to} not found")).pure[F]

        case Response(_, code, _, _, _, bodyText) if code.code == 500 =>
          logger.error(s"OneFrame internal error (500): $bodyText") *>
            Left(Error.OneFrameServerError(s"OneFrame internal error: $bodyText")).pure[F]

        case Response(_, code, _, _, _, bodyText) =>
          logger.error(s"Unexpected response from OneFrame ($code): $bodyText") *>
            Left(Error.OneFrameUnexpectedStatus(code.code, bodyText.toString())).pure[F]
      }
    } yield result
  }
}

object OneFrameClient {

  def apply[F[_]: Async](oneFrameClientConfiguration: OneFrameClientConfiguration)(
      implicit backend: Backend[F],
      logger: Logger[F]
  ): Algebra[F] = new OneFrameClient[F](oneFrameClientConfiguration)

}
