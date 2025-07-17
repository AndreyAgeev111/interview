package forex.programs.rates.error

import forex.services.rates.error.Errors.{ Error => RatesServiceError }

object Errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error {
      override def getMessage: String = msg
    }
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) =>
      Error.RateLookupFailed(Option(msg).getOrElse("Unknown error from OneFrame"))
    case other =>
      Error.RateLookupFailed(s"Unhandled error: ${Option(other).map(_.toString).getOrElse("null")}")
  }
}