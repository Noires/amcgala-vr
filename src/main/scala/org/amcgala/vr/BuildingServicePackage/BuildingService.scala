package org.amcgala.vr.BuildingServicePackage

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.amcgala.CellType
import org.amcgala.vr.BuildingServicePackage.BuildingTemplate.Blueprint
import org.amcgala.vr.building.TownHall.Contract
import org.amcgala.vr.building.{LivingQuarters, TownHall}
import org.amcgala.vr.{Bot, Coordinate, MultiStepTask}

import scala.collection.immutable.ListSet
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import org.amcgala.vr.{Main, Cell, Task}


/**
 * Created by root on 6/4/14.
 */
object BuildingService {

  case class Test(string: String)

  case class JobDone(coordinate: Coordinate)

  class BuildBuildingTaskV1(building: String, start: Coordinate, end: Coordinate)(implicit val bot: Bot) extends MultiStepTask {
    override type Return = Coordinate
    private val quart_temp = Array(Coordinate(-1, 0), Coordinate(0, 0), Coordinate(1, 0), Coordinate(-1, -1), Coordinate(0, -1), Coordinate(1, -1), Coordinate(-1, -2), Coordinate(0, -2), Coordinate(1, -2))

    var p = Array(Coordinate(0,0), Coordinate(-1,-3),Coordinate(-1, -4), Coordinate(-1, -5), Coordinate(-1, -6), Coordinate(-1, -7), Coordinate(0, -8), Coordinate(-2, -2), Coordinate(-3, -1), Coordinate(-3, 0), Coordinate(-2,1), Coordinate(-1,1),
      Coordinate(1, 1), Coordinate(2,1), Coordinate(2, -2),Coordinate(3,0), Coordinate(3,-1),  Coordinate(1, -3), Coordinate(1, -4), Coordinate(1, -5), Coordinate(1, -6), Coordinate(1, -7), Coordinate(1, -3) )

    //val blueprint = new BuildingTemplate(building).transpose(start, 90).toArray
    var all = ListSet[Coordinate]()
    for(a <- 0 to p.size-1) {
      all += SimpleCalculus.rotate(0, p(a))
    }
    val blueprint = SimpleCalculus.translate(all, Coordinate(20, 40)).toArray
    var time = 0
    var index = 0

    override def onTick(): Unit = {
      if (time % 6 == 0) {
        if (index < blueprint.length) {
          val current = Coordinate(blueprint(index).x, blueprint(index).y)
          bot.moveToPosition(current)
          if (current == Coordinate(start.x, start.y)) Main.simulation.spawnBuilding(classOf[LivingQuarters], current)
          else Main.simulation.changeCellType(current, CellType.Wall)
          index = index + 1
        }
        else {
          done()
          result success start
        }
      }
      time = time + 1
    }

    override def execute(): Future[Return] = result.future
  }


  class BuildBuildingTaskV2(building: String, blueprint: Blueprint)(implicit val bot: Bot) extends MultiStepTask {
    override type Return = Coordinate
    val bp = blueprint.print.toArray
    var time = 0
    var index = 0

    override def onTick(): Unit = {
      if (time % 6 == 0) {
        if (index < bp.length) {
          val current = Coordinate(bp(index).x, bp(index).y)
          bot.moveToPosition(current)
          if (current == Coordinate(blueprint.door.x, blueprint.door.y)) Main.simulation.spawnBuilding(classOf[LivingQuarters], current)
          else Main.simulation.changeCellType(current, CellType.Wall)
          index = index + 1
        }
        else {
          done()
          result success blueprint.door
        }
      }
      time = time + 1
    }

    override def execute(): Future[Return] = result.future
  }


  class ScoutLocationTask(building: String)(implicit val bot: Bot) extends MultiStepTask {
    val bld = new BuildingTemplate(building)
    var buildPoint = Coordinate(0, 0)
    var whole = ListSet[Set[Coordinate]]()
    var crumbs = new mutable.ArrayStack[Coordinate]()

    override type Return = Blueprint

    def scale(): BuildingTemplate.Scale = {
      building match {
        case "hospital" => bld.hospital
        case "diner" => bld.diner
        case _ => bld.livingQuarter
      }
    }

    def direction(progress: Int): Coordinate =
      progress match {
        case 0 => Coordinate(-1, 0)
        case 1 => Coordinate(0, 1)
        case 2 => Coordinate(1, 0)
        case 3 => Coordinate(0, -1)
      }

    def determine(current: Coordinate, angle: Int, spectrum: Map[Coordinate, Cell]): Unit = {
      val offsetStart = Coordinate(current.x + 1, current.y - 1)
      var transCoordinate = if (angle == 90) calibrate(bld.transpose(offsetStart, angle), scale().m); else bld.transpose(offsetStart, angle)

      var orientation = if (angle == 90) scale().m; else scale().n
      var newSet = Set[Coordinate]()
      for (a <- 0 to 3) {
        for (shift <- 0 to orientation + 1) {
          newSet = newSet.empty
          bld.shift(transCoordinate, direction(a), shift).foreach {
            c =>
              spectrum.foreach {
                s =>
                  if (c.x == s._1.x && c.y == s._1.y && s._2.cellType == CellType.Floor) {
                    newSet += c
                  }
              }
          }
          if (shift == orientation + 1) {
            transCoordinate = bld.shift(transCoordinate, direction(a), shift)
            if (orientation == scale().m) orientation = scale().n; else orientation = scale().m
          }
          if (newSet.size == transCoordinate.size) {
            whole += newSet
          }
        }
      }
    }

    def calibrate(set: ListSet[Coordinate], width: Int): ListSet[Coordinate] = {
      var ns = ListSet[Coordinate]()
      set.foreach {
        point =>
          ns += Coordinate(point.x + (width - 1), point.y)
      }
      ns
    }

    def color(set: Set[Coordinate]): Unit = {
      set.foreach {
        el =>
          Main.simulation.changeCellType(Coordinate(el.x, el.y), CellType.Grass)
      }
    }

    def scout(current: Coordinate, spectrum: Map[Coordinate, Cell]): Boolean = {
      /*for (angle <- 0 to 90) {
          determine(current, angle, spectrum)
      }
      whole = whole.filter(p => p.size == scale().n * scale().m)*/
      whole.size != 0
    }

    def findDoor(set: Set[Coordinate], Coordinate: Coordinate): Unit = {
      set.foreach {
        coord =>
          val deltax = coord.x - Coordinate.x
          val deltay = coord.y - Coordinate.y
          if (deltax <= 1 && deltax >= -1 && deltay <= 1 && deltay >= -1) buildPoint = coord
      }
    }


    override def onTick(): Unit = {
      for {
        pos <- bot.position()
        near_spectrum <- bot.visibleCells(1.5)
        wide_spectrum <- bot.visibleCells(scale().n + 3)
      } yield {
        var count = 0
        var pts = pos
        if (scout(Coordinate(pos.x, pos.y), wide_spectrum)) {
          findDoor(whole.head, Coordinate(pos.x, pos.y))
          done()
          result success Blueprint(buildPoint, whole.head)
        }
        else {
          val last = if(crumbs.nonEmpty) crumbs.pop(); else pos
          val map = near_spectrum.filter(p => p._2.cellType == CellType.Road && p._1 != pos && p._1 != last)
          map.foreach {
            elem =>
              if (!crumbs.contains(elem._1)) {
                pts = elem._1
              }
              else {
              count = count + 1
              if (count == map.size) {
                  pts = crumbs.pop()
                  println("DeadEnd")
                }
            }
          }
          crumbs.push(last)
          crumbs.push(pts)
          crumbs.distinct
          bot.moveToPosition(pts)
        }
      }
    }

    override def execute(): Future[Return] = result.future

  }

  class AlertNPCTask(ref: ActorRef, point: Coordinate)(implicit val bot: Bot) extends Task {
    override type Return = Unit.type

    override def isDone(): Boolean = done

    var done = false

    override def execute(): Future[Return] = Future[Return] {
      ref ! JobDone(point)
      Unit
    }
  }

  class FindRoadTask()(implicit val bot: Bot) extends MultiStepTask {
    override type Return = Coordinate

    override def execute(): Future[Return] = result.future

    override def onTick(): Unit = {
      for {
        pos <- bot.position()
        spectrum <- bot.visibleCells(2)
      } yield {
        spectrum.foreach {
          elm =>
            if (elm._2.cellType == CellType.Road) {
              done()
              result success elm._1
            }
        }
        bot.moveForward()
      }
    }
  }


  class AlertTownHallTask(building: String, npc: Bot)(implicit val bot: Bot) extends Task {
    override type Return = Unit.type

    override def isDone(): Boolean = done

    var done = false
    val map = bot.vicinity(2)

    override def execute(): Future[Return] =
      for (m <- map) yield {
        m.buildings.keys.foreach(ref => ref ! TownHall.RegisterBuilding(building, npc))
        done = true
        Unit
      }
  }


  class RetrieveContractTask()(implicit val bot: Bot) extends MultiStepTask {
    override type Return = Contract

    implicit val timeout = Timeout(5.seconds)

    val map = bot.vicinity(2)
    var contract: Contract = Contract(null, null)
    var retrieved = false

    override def onTick(): Unit = {
      if (!retrieved) {
        for (m <- map) m.buildings.keys.foreach {
          ref =>
            for (cont <- (ref ? TownHall.RequestContract).mapTo[Contract]) {
              if (cont.building != "No Contract") {
                println(cont.building)
                contract = cont
                retrieved = true
              }
            }
        }
      }
      else {
        done()
        result success contract
      }
    }

    def execute(): Future[Return] = result.future
  }


  // SIMPLE BEHAVIOR
  class RequestBuildingTask(building: String, bot_t: Bot) extends Task {

    override type Return = Unit.type
    override val bot: Bot = bot_t

    override def isDone(): Boolean = done

    val map = bot.vicinity(2)
    var done = false

    override def execute(): Future[Return] =
      for {
        m <- map
      } yield {
        m.buildings.keys.foreach(ref => ref ! TownHall.RequestBuilding(building, bot))
        done = true
        Unit
      }
  }

  def retrieveContract()(implicit bot: Bot) = new RetrieveContractTask()

  def requestBuilding(building: String, bot: Bot) = new RequestBuildingTask(building, bot)

  def scoutLocation(building: String)(implicit bot: Bot) = new ScoutLocationTask(building)

  def buildBuildingV1(building: String, start: Coordinate, end: Coordinate)(implicit bot: Bot) = new BuildBuildingTaskV1(building, start, end)

  def buildBuildingV2(building: String, blueprint: Blueprint)(implicit bot: Bot) = new BuildBuildingTaskV2(building, blueprint)

  def alertTownHall(building: String, npc: Bot)(implicit bot: Bot) = new AlertTownHallTask(building, bot)

  def alertNPC(ref: ActorRef, home: Coordinate)(implicit bot: Bot) = new AlertNPCTask(ref, home)

  def findRoad()(implicit bot: Bot) = new FindRoadTask()
}
