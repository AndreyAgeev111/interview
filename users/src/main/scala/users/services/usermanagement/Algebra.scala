package users.services.usermanagement

import users.domain._

trait Algebra[F[_]] {
  import User._

  def generateId(): F[Id]

  def get(
      id: Id
  ): F[error.Error Either User]

  def signUp(
      userName: UserName,
      emailAddress: EmailAddress,
      password: Option[Password]
  ): F[error.Error Either User]

  def updateEmail(
      id: Id,
      emailAddress: EmailAddress
  ): F[error.Error Either User]

  def updatePassword(
      id: Id,
      password: Password
  ): F[error.Error Either User]

  def resetPassword(
      id: Id
  ): F[error.Error Either User]

  def block(
      id: Id
  ): F[error.Error Either User]

  def unblock(
      id: Id
  ): F[error.Error Either User]

  def delete(
      id: Id
  ): F[error.Error Either Done]

  def all(): F[error.Error Either List[User]]

}
