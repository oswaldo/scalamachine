package code

import scalaz.syntax.monad._
import scalamachine.servlet.ServletRoutingTable
import scalamachine.core.dispatch.Route._
import scalamachine.core.{ValueRes, ReqRespData, Resource}
import scalamachine.core.dispatch.RoutingTable

object DefaultResource extends Resource {
  // fake a context since we don't use any
  type Context = Option[Nothing]
  def init = None 
}

object UnavailableResource extends Resource {
  // fake a context since we don't use any
  type Context = Option[Nothing]
  def init = None 
  override def serviceAvailable = false.point[Result]
}

class ExampleRoutes extends ServletRoutingTable {
  def routes = RoutingTable(
    pathMatching {
      "unavailable"
    } serve UnavailableResource,
    pathMatching {
      "default"
    } serve DefaultResource
  )
}
