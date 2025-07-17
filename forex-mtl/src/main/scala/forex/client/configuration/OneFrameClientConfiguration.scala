package forex.client.configuration

import scala.concurrent.duration.FiniteDuration

case class OneFrameClientConfiguration(baseUrl: String, timeout: FiniteDuration, token: String)
