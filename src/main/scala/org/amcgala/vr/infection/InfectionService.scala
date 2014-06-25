package org.amcgala.vr.infection

import org.amcgala.vr.{Position, MultiStepTask, Bot}
import akka.actor.ActorRef
import org.amcgala.agent.Simulation
import org.amcgala.vr.BotAgent.RegisterNeed

object InfectionService {

class FindNextBotToInfectTask(implicit val bot: Bot) extends MultiStepTask {
  import scala.concurrent._
  import scala.concurrent.ExecutionContext.Implicits.global

  type Return = Boolean

  override def onTick(): Unit = {
    // Anderer Bot im Suchbereich.
    for(v <- bot.vicinity(20)){
      if(v.nonEmpty){
        bot.moveToPosition(v.last._2)
        // Anderer Bot in unmittelbarer Umgebung.
        for (i <- bot.vicinity(1))
        {
          if (i.nonEmpty){
            // TODO: Möglichkeit um bei ActorRef-Bedürfniss zu registrieren.
            if (i.last._1.isInstanceOf[Bot]){
              i.last._1.asInstanceOf[Bot].registerNeed(Infection.apply())
              done()
            }
          }
        }
      }else {
        // Wait
      }
    }
    done()
    result success true
  }

  def execute(): Future[Return] =  result.future
}
  def findNextBotToInfect(implicit bot: Bot) = new FindNextBotToInfectTask()
}