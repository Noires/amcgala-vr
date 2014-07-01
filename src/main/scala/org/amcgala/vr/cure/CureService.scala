package org.amcgala.vr.infection

import org.amcgala.vr.{Task, Position, MultiStepTask, Bot}
import org.amcgala.vr._
import akka.actor.ActorRef
import org.amcgala.agent.Simulation
import org.amcgala.vr.BotAgent.RegisterNeed
import org.amcgala.vr.building.BuildingType
import scala.concurrent.Future

object CureService {

  class getCuredTask(implicit val bot: Bot, doctor: Bot) extends Task {
    type Return = Boolean
    import scala.concurrent._

    def isDone() = true

    def execute(): Future[Return] = {
      bot.removeNeed(Infection.ID)
      future{true}
    }

  }

  def getCured(implicit bot: Bot, doctor:Bot) = new getCuredTask()
}
