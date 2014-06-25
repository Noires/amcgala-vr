package org.amcgala.vr.building

import org.amcgala.vr.building.Diner.{BillRequest, FoodRequest}

object Diner {
  case object FoodRequest
  case object BillRequest
}

class Diner extends Building {

  override def taskHandling: Receive = {
    case FoodRequest => ???
    case BillRequest => ???
  }
}
