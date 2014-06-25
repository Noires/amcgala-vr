package org.amcgala.vr.infection

import org.amcgala.vr.{Behavior, Position, BotAgent, Bot}
import org.amcgala.vr.need.{ Need, SatisfactionBehavior }
import example.LocationService
import org.amcgala.vr.building.BuildingType.Restaurant
import org.amcgala.vr.need.Needs.Hunger
import akka.actor.ActorRef
import example.LocationService.Coordinate
import scala.util.Random
import scala.concurrent.Future

class InfectionBehavior()(implicit val bot: Bot) extends Behavior {

  import scala.concurrent.ExecutionContext.Implicits.global

  type Return = Boolean

  private val target = Coordinate(Random.nextInt(200), Random.nextInt(200))

  def start() = {
    for {
      map ← bot.executeTask(InfectionService.findNextBotToInfect(bot))
     // ho <- map.head
     // pos = new LocationService.Coordinate(map.head._2.x, map.head._2.y)
     // mcd ← bot.executeTask(LocationService.walkTo(pos)(bot))
    } yield {
      done = true
      //need.decrease(49)
      map
    }

  }

}
