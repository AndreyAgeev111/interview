package forex.util.configuration

import scala.concurrent.duration.FiniteDuration

case class RetryConfiguration(retryDuration: FiniteDuration, amount: Int)