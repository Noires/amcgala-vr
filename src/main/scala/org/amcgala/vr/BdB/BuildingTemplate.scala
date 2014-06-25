package org.amcgala.vr.BdB

import org.amcgala.vr.BdB.BuildingTemplate.Scale
import org.amcgala.vr.{Coordinate, Simulation}
import scala.collection.immutable.ListSet
import org.amcgala.vr.BdB.SimpleCalculus.{dot, vector, rotate, translate, round, icos, magnitude}

object BuildingTemplate {
  case class Blueprint(door: Coordinate, print: Set[Coordinate])
  case class Scale(n: Int, m: Int)
}


class BuildingTemplate(building: String) {
  // UPSIDEDOWN TEMPLATE BECAUSE OF UPSIDEDOWN COORDINATE SYSTEM

  //private val quart_temp = Array(Coordinate(-1, 0), Coordinate(0, 0), Coordinate(1, 0), Coordinate(-1, -1), Coordinate(0, -1), Coordinate(1, -1), Coordinate(-1, -2), Coordinate(0, -2), Coordinate(1, -2))

  val livingQuarter = Scale(3, 2)
  val diner = Scale(5, 3)
  val hospital = Scale(7, 5)

  val quart_temp = Array(Coordinate(0, 0), Coordinate(1, 0),Coordinate(2, 0), Coordinate(0, -1), Coordinate(1, -1), Coordinate(2, -1))

  val hosp_temp = Array(Coordinate(0, 0),  Coordinate(1, 0),  Coordinate(2, 0), Coordinate(3, 0), Coordinate(4, 0), Coordinate(5, 0), Coordinate(6, 0),
                        Coordinate(0, -1), Coordinate(1, -1), Coordinate(2, -1), Coordinate(3, -1), Coordinate(4, -1), Coordinate(5, -1), Coordinate(6, -1),
                        Coordinate(0, -2), Coordinate(1, -2), Coordinate(2, -2), Coordinate(3, -2), Coordinate(4, -2), Coordinate(5, -2), Coordinate(6, -2),
                        Coordinate(0, -3), Coordinate(1, -3), Coordinate(2, -3), Coordinate(3, -3), Coordinate(4, -3), Coordinate(5, -3), Coordinate(6, -3),
                        Coordinate(0, -4), Coordinate(1, -4), Coordinate(2, -4), Coordinate(3, -4), Coordinate(4, -4), Coordinate(5, -4), Coordinate(6, -4))

  val din_temp = Array(Coordinate(0, 0), Coordinate(1, 0), Coordinate(2, 0), Coordinate(3, 0), Coordinate(4, 0),
                       Coordinate(0, -1), Coordinate(1, -1), Coordinate(2, -1), Coordinate(3, -1), Coordinate(4, -1),
                       Coordinate(0, -2), Coordinate(1, -2), Coordinate(2, -2), Coordinate(3, -2), Coordinate(4, -2))


  var p = Array(Coordinate(0,0), Coordinate(-1,-3),Coordinate(-1, -4), Coordinate(-1, -5), Coordinate(-1, -6), Coordinate(-1, -7), Coordinate(0, -8), Coordinate(-2, -2), Coordinate(-3, -1), Coordinate(-3, 0), Coordinate(-2,1), Coordinate(-1,1),
    Coordinate(1, 1), Coordinate(2,1), Coordinate(2, -2),Coordinate(3,0), Coordinate(3,-1),  Coordinate(1, -3), Coordinate(1, -4), Coordinate(1, -5), Coordinate(1, -6), Coordinate(1, -7), Coordinate(1, -3) )


  def findAngle(start: Coordinate, end: Coordinate): Int = {
    val vec1 = vector(start, end)
    val vec2 = vector(start, Coordinate(end.x, start.y))

    round(icos(dot(vec1, vec2) / (magnitude(vec1) * magnitude(vec2))))
  }

  def defTemp(): Array[Coordinate] = {
    building match {
      case "hospital" => hosp_temp
      case "diner" => din_temp
      case _ => quart_temp
    }
  }

  def createBlueprint(start: Coordinate, end: Coordinate): Set[Coordinate] = {
    val angle = -1 * findAngle(start, end) //negate because of inversion
    var rotated = ListSet[Coordinate]()

    for (a <- 0 to defTemp().length-1) {
      rotated += rotate(angle, defTemp()(a))
    }
    translate(rotated, start)
  }

  def transpose(start: Coordinate, angle: Int): ListSet[Coordinate] = {
    var rotated = ListSet[Coordinate]()

    for (a <- 0 to defTemp().length-1) {
      rotated += rotate(-angle, defTemp()(a))
    }
    translate(rotated, start)
  }

  def shift(template: ListSet[Coordinate], direction: Coordinate, magnitude: Int): ListSet[Coordinate] = {
    var s = ListSet[Coordinate]()
    template.foreach {
      coord =>
        s += Coordinate(coord.x+(direction.x*magnitude), coord.y+(direction.y*magnitude))
    }
    s
  }

}
