package forex

package object client {
  type RatesClient[F[_]] = client.Algebra[F]
  final val RatesClient = client.OneFrameClient
}
