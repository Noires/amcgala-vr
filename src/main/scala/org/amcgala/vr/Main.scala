package org.amcgala.vr

import scala.util.Random
import example.{BresenhamIterator, LocationService}
import example.LocationService.Coordinate
import scala.concurrent.Future
import org.amcgala.vr.building.TownHall
import org.amcgala.vr.building.BuildingType.Restaurant
import org.amcgala.CellType
import org.amcgala.vr.need.{ Need, SatisfactionBehavior }
import org.amcgala.vr.need.Needs.Hunger
import org.amcgala.vr.infection.InfectionBehavior

/**
  * Startet die Simulation.
  */
object Main extends App {
  val simulation = new Simulation(200, 200)

  for (i ← 0 until 15) {
    simulation.spawnBot(classOf[SimpleNPC], Position(Random.nextInt(simulation.width), Random.nextInt(simulation.height)))
  }

  for (i ← 0 until 15) {
    simulation.spawnBot(classOf[SimpleSicknessSpreader], Position(Random.nextInt(simulation.width), Random.nextInt(simulation.height)))
  }

  simulation.spawnBuilding(classOf[TownHall], Position(100, 100))
  for (x ← 50 until 150) {
    simulation.changeCellType(Position(x, 98), CellType.Road)
  }
}

class SimpleNPC extends BotAgent {
  // Rumstehen.
}

class SimpleSicknessSpreader extends BotAgent {
  brain.registerJob(new InfectionBehavior())
  brain.registerIdleBehavior(new RandomWalkBehavior())
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

  type Return = LocationService.Cell

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