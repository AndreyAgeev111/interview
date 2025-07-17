package forex.config

import forex.client.configuration.OneFrameClientConfiguration
import forex.util.configuration.RetryConfiguration

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrameClientConfiguration: OneFrameClientConfiguration,
    retryConfiguration: RetryConfiguration
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
