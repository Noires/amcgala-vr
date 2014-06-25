package org.amcgala.vr.cure

import org.amcgala.vr.{Behavior, BotAgent, Bot}
import org.amcgala.vr.need.{ Need, SatisfactionBehavior }
import example.LocationService
import org.amcgala.vr.building.BuildingType.Restaurant
import org.amcgala.vr.need.Needs.Hunger
import akka.actor.ActorRef
import scala.util.Random
import scala.concurrent.Future
import org.amcgala.vr.building.{Hospital, BuildingType, Building}
import org.amcgala.vr.infection.{Infection, CureService}

class CureBehavior()(implicit val bot: Bot) extends SatisfactionBehavior{
    import scala.concurrent.ExecutionContext.Implicits.global

    type Return = Boolean

    def start() = {
      for{
        cord <- bot.executeTask(LocationService.findLocation(BuildingType.Hospital))
        cell <- bot.executeTask(LocationService.walkTo(cord))
        //doctor <- bot.executeTask((bot, Job.Doctor))
        result <- bot.executeTask(CureService.getCured(bot))
      } yield result
    }

    val need = Hunger
}
