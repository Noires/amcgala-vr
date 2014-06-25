package org.amcgala.vr.BdB


import org.amcgala.vr.Coordinate

import scala.collection.immutable.ListSet

/**
 * Created by root on 6/15/14.
 */
object SimpleCalculus {

  def magnitude(point: Coordinate): Double = Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2))

  def dot(point1: Coordinate, point2: Coordinate): Double = (point1.x * point2.x) + (point1.y * point2.y)

  def vector(point1: Coordinate, point2: Coordinate): Coordinate = Coordinate(point2.x - point1.x, point2.y - point1.y)

  def rotate(angle: Double, point: Coordinate): Coordinate = {
    val mat = rotmx(angle)
    val x = round(mat(0) * point.x + mat(1) * point.y)
    val y = round(mat(2) * point.x + mat(3) * point.y)
    Coordinate(x, y)
  }

  def icos(number: Double): Double = Math.acos(number) * (180/Math.PI)

  def round(number: Double): Int = {
    Math.round(number).toInt
  }

  def rotmx(angle: Double): Array[Double] = Array(Math.cos(angle.toRadians), -Math.sin(angle.toRadians), Math.sin(angle.toRadians), Math.cos(angle.toRadians))

  def translate(mat: ListSet[Coordinate], point: Coordinate): ListSet[Coordinate] =  {
    var translation = ListSet[Coordinate]()
    mat.foreach {
      elem =>
        translation += Coordinate(elem.x + point.x, elem.y + point.y)
    }
    translation
  }

}
