package org.amcgala.vr.BuildingServicePackage

import scala.collection.immutable.ListSet
import scala.concurrent.ExecutionContext.Implicits.global
import org.amcgala.vr._
import scala.concurrent.Future
import example.{BresenhamIterator, LocationService}
import org.amcgala.CellType

/**
 * Created by root on 6/8/14.
 */
object BuildRoad {

  private def bresenham(x0: Double, y0: Double, x1: Double, y1: Double): Coordinate = {

    import scala.math.abs
    var x = x0
    var y = y0

    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)

    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1

      var err = dx - dy
        val e2 = 2 * err
        if (e2 > -dy) {
          err -= dy
          x += sx
        }
        if (e2 < dx) {
          err += dx
          y += sy
        }
        Coordinate(x, y)
  }

  def buildRoad(start: Coordinate, finish: Coordinate): Unit = {
    var cur = start
    while(cur.x != finish.x || cur.y != finish.y) {
      val point = bresenham(cur.x, cur.y, finish.x , finish.y)
      Main.simulation.changeCellType(point, CellType.Road)
      cur = Coordinate(point.x, point.y)
    }
  }

 case class P(x: Double, y: Double)

  def translate(start: P, scale: Int): ListSet[P] = {
    val points = ListSet(P(22, 6), P(22, 14), P(16, 14), P(13, 19), P(5, 19), P(5, 12), P(12, 8), P(-1, 7))
    var np = ListSet[P]()
    points.foreach {
      point =>
        val temp = P((point.x + start.x)*scale, (point.y + start.y)*scale)
        np += temp
    }
    np
  }

  def buildInfrastructure(start: Coordinate, scale: Int): Unit = {
    val points = translate(P(start.x, start.y), scale)
    var tmp: P = P(0, 0)
    points.foreach {
      point =>
        if(point == points.head) {
          buildRoad(Coordinate(start.x*scale, start.y*scale), Coordinate(point.x, point.y))
          tmp = point
        }
        if(point == points.last) {
          buildRoad(Coordinate(tmp.x, tmp.y), Coordinate(point.x, point.y))
          buildRoad(Coordinate(point.x, point.y), Coordinate(start.x*scale, start.y*scale))
        }
        else {
          buildRoad(Coordinate(tmp.x, tmp.y), Coordinate(point.x, point.y))
          tmp = point
        }
    }
  }
}
