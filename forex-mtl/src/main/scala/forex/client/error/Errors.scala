package forex.client.error

import cats.MonadError

object Errors {
  sealed trait Error extends Product with Serializable

  object Error {

    final case class OneFrameNotFound(message: String) extends Error
    final case class OneFrameUnauthorized(message: String) extends Error
    final case class OneFrameServerError(message: String) extends Error
    final case class OneFrameDecodeFailed(message: String) extends Error
    final case class OneFrameUnexpectedStatus(code: Int, body: String) extends Error

  }

  implicit def monadErrorInstance[F[_]](implicit F: MonadError[F, Throwable]): MonadError[F, Error] =
    new MonadError[F, Error] {
      override def raiseError[A](e: Error): F[A] =
        F.raiseError(new RuntimeException(e.toString))

      override def handleErrorWith[A](fa: F[A])(f: Error => F[A]): F[A] =
        F.handleErrorWith(fa) {
          case t: RuntimeException if t.getMessage.startsWith("OneFrame") =>
            f(Error.OneFrameDecodeFailed(t.getMessage))
          case other => F.raiseError(other)
        }

      override def pure[A](x: A): F[A] = F.pure(x)

      override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = F.flatMap(fa)(f)

      override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = F.tailRecM(a)(f)
    }

}
