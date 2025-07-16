package users.services.util

import java.time.OffsetDateTime

trait TimeService {
  def now(): OffsetDateTime
}

class TimeServiceImpl() extends TimeService {
  override def now(): OffsetDateTime =
    OffsetDateTime.now()
}
