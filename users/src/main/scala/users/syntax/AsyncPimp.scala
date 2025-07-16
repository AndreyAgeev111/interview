package users.syntax

import cats.effect.Async

import scala.concurrent.Future

object AsyncPimp {
  implicit class AsyncOps[F[_]](private val F: Async[F]) extends AnyVal {
    def evalFuture[A](fa: => Future[A]): F[A] =
      F.fromFuture(F.delay(fa))
  }
}