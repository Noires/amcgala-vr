package org.amcgala.vr.infection

import org.amcgala.vr.{Task, MultiStepTask, Bot}
import org.amcgala.vr._
import akka.actor.ActorRef
import org.amcgala.agent.Simulation
import org.amcgala.vr.BotAgent.RegisterNeed
import org.amcgala.vr.building.BuildingType
import scala.concurrent.Future

object CureService {

  class getCuredTask(implicit val bot: Bot) extends Task {
    import scala.concurrent.ExecutionContext.Implicits.global

    type Return = Boolean
    import scala.concurrent._

    def isDone() = true

    def execute(): Future[Return] = {
      future{true}
    }

  }

  def getCured(implicit bot: Bot) = new getCuredTask()
}