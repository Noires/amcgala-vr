package org.amcgala.vr.cure

import org.amcgala.vr.{Behavior, Position, BotAgent, Bot}
import org.amcgala.vr.need.{ Need, SatisfactionBehavior }
import example.LocationService
import org.amcgala.vr.building.BuildingType.Restaurant
import org.amcgala.vr.need.Needs.Hunger
import akka.actor.ActorRef
import example.LocationService.Coordinate
import scala.util.Random
import scala.concurrent.Future
import org.amcgala.vr.infection.CureService
import org.amcgala.vr.building.BuildingType

class DoctorGoToWorkBehavior()(implicit val bot: Bot) extends Behavior {

  import scala.concurrent.ExecutionContext.Implicits.global

  type Return = Boolean

  def start() = {
    for {
      cord <- bot.executeTask(LocationService.findLocation(BuildingType.Hospital))
      cell <- bot.executeTask(LocationService.walkTo(cord))
    } yield {
      done = true
      true
    }
  }

  class DoctorCurePatientBehavior()(implicit val bot: Bot) extends Behavior {

    import scala.concurrent.ExecutionContext.Implicits.global

    type Return = Boolean

    def start() = {
      for {
        // Vicinity?
        // FindSickPerson? Benötigt NeedErkennung oder BedarfsAnfrage von anderem Bot
      } yield {
        done = true
        true
      }
    }

}
