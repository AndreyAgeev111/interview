package users

import users.services.usermanagement.Interpreters

package object services {

  type UserManagement[F[_]] = usermanagement.Algebra[F]
  val UserManagement: Interpreters.type = usermanagement.Interpreters

}
