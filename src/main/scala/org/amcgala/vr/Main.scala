package org.amcgala.vr

import akka.actor.Actor.Receive
import scala.util.Random
import example.{ BresenhamIterator, LocationService }
import scala.concurrent.Future
import org.amcgala.vr.building.BuildingType.Restaurant
import org.amcgala.vr.need.{Need, SatisfactionBehavior}
import org.amcgala.vr.need.Needs.Hunger
import org.amcgala.vr.task.BobBuilder
import org.amcgala.vr.BdB.BuildRoad

/**
 * Startet die Simulation.
 */
object Main extends App {
  val simulation = new Simulation(200, 200)

  for (i ← 0 until 1) {
    simulation.spawnBot(classOf[SimpleNPC], Coordinate(70,100))
    simulation.spawnBot(classOf[tempNPC], Coordinate(100, 40))
  }


  //BuildRoad.buildRoad(Coordinate(43, 69), Coordinate(52, 78))
  BuildRoad.buildInfrastructure(Coordinate(1,1), 8)
}

class SimpleNPC extends BotAgent {
  brain.registerJob(new BobBuilder())
  brain.registerIdleBehavior(new RandomWalkBehavior())
}

class tempNPC extends BotAgent {
  brain.registerJob(new GoAndGet())
  brain registerIdleBehavior(new RandomWalkBehavior())

  override var customReceive: Receive = {
    case BuildingService.JobDone(coordinate) =>
      println("Bot alerted")

  }
}

class GoAndGet() (implicit val bot: Bot) extends Behavior {

  import scala.concurrent.ExecutionContext.Implicits.global

  override type Return = Unit.type

  override def start(): Future[Return] = {
    for {
      loc <- bot.townHall
      q <- bot.executeTask(LocationService.walkTo(Coordinate(loc.x, loc.y-1)))
      diner <- bot.executeTask(BuildingService.requestBuilding("diner", this.bot))
      house <- bot.executeTask(BuildingService.requestBuilding("livingQuarter", this.bot))
      hospital <- bot.executeTask(BuildingService.requestBuilding("hospital", this.bot))
    } yield {
      Unit
    }
  }
}

class RandomWalkBehavior()(implicit val bot: Bot) extends Behavior {

  import scala.concurrent.ExecutionContext.Implicits.global

  type Return = Unit.type
  private val target = Coordinate(Random.nextInt(200), Random.nextInt(200))

  def start(): Future[Return] = {
    for (t ← bot.executeTask(LocationService.walkTo(target)(bot))) yield {
      done = true
      Unit
    }
  }
}

class JobBehavior()(implicit val bot: Bot) extends SatisfactionBehavior {

  import scala.concurrent.ExecutionContext.Implicits.global

  type Return = Cell


  def start() = {
    for {
      pos ← bot.executeTask(LocationService.findLocation(Restaurant)(bot))
      mcd ← bot.executeTask(LocationService.walkTo(pos)(bot))
    } yield {
      done = true
      need.decrease(49)
      mcd
    }
  }

  val need: Need = Hunger()
}