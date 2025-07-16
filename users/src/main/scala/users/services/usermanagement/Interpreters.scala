package users.services.usermanagement

import java.util.UUID
import java.time.OffsetDateTime
import cats.data.EitherT
import cats.effect.Async
import cats.syntax.all._
import users.syntax.AsyncPimp._
import users.config._
import users.domain._
import users.persistence.repositories._

object Interpreters {
  def default[F[_]: Async](userRepository: UserRepository): Algebra[F] =
    new DefaultInterpreter[F](userRepository)

  def unreliable[F[_]: Async](underlying: Algebra[F], config: ServicesConfig.UsersConfig): Algebra[F] =
    new UnreliableInterpreter[F](underlying, config)
}

final class DefaultInterpreter[F[_]: Async](repo: UserRepository) extends Algebra[F] {

  override def generateId(): F[User.Id] =
    Async[F].delay(UUID.randomUUID().toString).map(User.Id(_))

  override def get(id: User.Id): F[error.Error Either User] =
    Async[F].evalFuture(repo.get(id)).map(_.toRight(error.Error.NotFound))

  override def signUp(userName: UserName, email: EmailAddress, password: Option[Password]): F[error.Error Either User] =
    (for {
      maybe <- EitherT.liftF(Async[F].evalFuture(repo.getByUserName(userName)))
      id <- EitherT.liftF(generateId())
      user <- EitherT.fromEither[F] {
               if (maybe.nonEmpty) Left(error.Error.Exists)
               else Right(User(id, userName, email, password, OffsetDateTime.now()))
             }
      _ <- EitherT(save(user))
    } yield user).value

  override def updateEmail(id: User.Id, email: EmailAddress): F[error.Error Either User] =
    update(id)(_.updateEmailAddress(email, OffsetDateTime.now()))

  override def updatePassword(id: User.Id, password: Password): F[error.Error Either User] =
    update(id)(_.updatePassword(password, OffsetDateTime.now()))

  override def resetPassword(id: User.Id): F[error.Error Either User] =
    update(id)(_.resetPassword(OffsetDateTime.now()))

  override def block(id: User.Id): F[error.Error Either User] =
    get(id).flatMap {
      case Right(user) if user.isDeleted => Async[F].pure(Left(error.Error.Deleted))
      case Right(user) if user.isBlocked => Async[F].pure(Left(error.Error.Blocked))
      case Right(user) =>
        val updated = user.block(OffsetDateTime.now())
        save(updated).as(Right(updated))
      case Left(err) => Async[F].pure(Left(err))
    }

  override def unblock(id: User.Id): F[error.Error Either User] =
    get(id).flatMap {
      case Right(user) if user.isDeleted => Async[F].pure(Left(error.Error.Deleted))
      case Right(user) if user.isActive  => Async[F].pure(Left(error.Error.Active))
      case Right(user) =>
        val updated = user.unblock(OffsetDateTime.now())
        save(updated).as(Right(updated))
      case Left(err) => Async[F].pure(Left(err))
    }

  override def delete(id: User.Id): F[error.Error Either Done] =
    get(id).flatMap {
      case Right(user) if user.isDeleted => Async[F].pure(Left(error.Error.Deleted))
      case Right(user) if user.isActive  => Async[F].pure(Left(error.Error.Active))
      case Right(user) =>
        val updated = user.delete(OffsetDateTime.now())
        save(updated).as(Right(Done))
      case Left(err) => Async[F].pure(Left(err))
    }

  override def all(): F[error.Error Either List[User]] =
    Async[F].evalFuture(repo.all()).map(Right(_))

  private def save(user: User): F[error.Error Either Done] =
    Async[F].evalFuture(repo.insert(user)).map(Right(_))

  private def update(id: User.Id)(f: User => User): F[error.Error Either User] =
    get(id).flatMap {
      case Right(user) =>
        val updated = f(user)
        save(updated).as(Right(updated))
      case Left(err) => Async[F].pure(Left(err))
    }
}

final class UnreliableInterpreter[F[_]: Async](underlying: Algebra[F], config: ServicesConfig.UsersConfig)
    extends Algebra[F] {
  import scala.util.Random
  import cats.syntax.all._
  import scala.concurrent.duration._
  import cats.effect.Temporal

  private def failOrTimeout[A](fa: F[A]): F[A] = {
    val fail    = Random.nextDouble() < config.failureProbability
    val timeout = Random.nextDouble() < config.timeoutProbability

    if (fail) Async[F].raiseError(new Exception("Injected failure"))
    else if (timeout) Temporal[F].sleep(365.days) *> fa
    else fa
  }

  override def generateId(): F[User.Id] = underlying.generateId()

  override def get(id: User.Id): F[error.Error Either User] = failOrTimeout(underlying.get(id))

  override def signUp(u: UserName, e: EmailAddress, p: Option[Password]): F[error.Error Either User] =
    failOrTimeout(underlying.signUp(u, e, p))

  override def updateEmail(id: User.Id, email: EmailAddress): F[error.Error Either User] =
    failOrTimeout(underlying.updateEmail(id, email))

  override def updatePassword(id: User.Id, p: Password): F[error.Error Either User] =
    failOrTimeout(underlying.updatePassword(id, p))

  override def resetPassword(id: User.Id): F[error.Error Either User] = failOrTimeout(underlying.resetPassword(id))

  override def block(id: User.Id): F[error.Error Either User] = failOrTimeout(underlying.block(id))

  override def unblock(id: User.Id): F[error.Error Either User] = failOrTimeout(underlying.unblock(id))

  override def delete(id: User.Id): F[error.Error Either Done] = failOrTimeout(underlying.delete(id))

  override def all(): F[error.Error Either List[User]] = failOrTimeout(underlying.all())
}
