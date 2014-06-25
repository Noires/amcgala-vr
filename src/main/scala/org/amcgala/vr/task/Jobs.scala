package org.amcgala.vr.task

import example.LocationService
import org.amcgala.vr.{Coordinate, BuildingService, Bot, Behavior}
import scala.concurrent.Future

class SicknessSpreader(val bot: Bot) extends Behavior {
  type Return = Unit.type

  def start(): Future[Return] = ???
}

class BobBuilder()(implicit val bot: Bot) extends Behavior {
  type Return = Unit.type

  import scala.concurrent.ExecutionContext.Implicits.global
  def start(): Future[Return] = {
    for {
      hall <- bot.townHall
      moveToHall <- bot.executeTask(LocationService.walkTo(Coordinate(hall.x, hall.y)))
      contract <- bot.executeTask(BuildingService.retrieveContract())
      road <- bot.executeTask(BuildingService.findRoad())
      stepOnRoad <- bot.executeTask(LocationService.walkTo(Coordinate(road.x, road.y)))
      blueprint <- bot.executeTask(BuildingService.scoutLocation(contract.building))
      build <- bot.executeTask(BuildingService.buildBuildingV2(contract.building, blueprint))
      goBackToHall <- bot.executeTask(LocationService.walkTo(Coordinate(hall.x, hall.y)))
      alertHall <- bot.executeTask(BuildingService.alertTownHall(contract.building, contract.bot))
      alertBot <- bot.executeTask(BuildingService.alertNPC(contract.bot.ref, blueprint.door))
      exitHall <- bot.executeTask(LocationService.walkTo(Coordinate(95, 100)))
    } yield {
      done = true
      Unit
    }
  }
}

class HouseMD(val bot: Bot) extends Behavior {
  type Return = this.type

  def start(): Future[Return] = ???
}