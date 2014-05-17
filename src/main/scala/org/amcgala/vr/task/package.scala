package org.amcgala.vr

import scala.concurrent.{ Promise, Future }
import org.amcgala.vr.BrainModes.BrainMode

object BrainModes {
  sealed trait BrainMode
  case object NeedMode extends BrainMode
  case object JobMode extends BrainMode
  case object IdleMode extends BrainMode
}

trait BrainModule { s: BotAgent ⇒
  val brain = new Brain(Bot(s.self))
  def registerNeed(need: Need) = brain.registerNeed(need)
  def registerJob(job: Behavior) = brain.registerJob(job)

  registerOnTickAction("update brain", () ⇒ {
    brain.update()
  })
}

trait Behavior {
  type Return
  val bot: Bot
  def isDone(): Boolean
  def start(): Future[Return]
}

trait Task {
  val bot: Bot
  type Return
  def isDone(): Boolean
  def execute(): Future[Return]
}

trait MultiStepTask extends Task {
  import scala.concurrent._

  var result: Promise[Return] = promise[Return]
  private var d = false

  val id = System.nanoTime().toString
  bot.registerOnTickAction(id, onTick)

  def onTick(): Unit

  def isDone() = d

  def done(): Unit = {
    d = true
    bot.removeOnTickAction(id)
  }
}

class Brain(bot: Bot) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private var job: Option[Behavior] = None
  private var idle: Option[Behavior] = None

  private var activeBehavior: Option[Behavior] = None
  private var activeTask: Option[Task] = None

  private var mode: BrainMode = BrainModes.IdleMode

  private val needManager = new NeedManager
  def executeTask(task: Task): Future[task.Return] = {
    activeTask = Some(task)
    task.execute()
  }
  def executeBehavior(behavior: Behavior): Future[behavior.Return] = {
    activeBehavior = Some(behavior)
    behavior.start()
  }

  def registerJob(jobBehavior: Behavior) = job = Some(jobBehavior)
  def registerIdleBehavior(behavior: Behavior) = idle = Some(behavior)
  def registerNeed(need: Need) = needManager.registerNeed(need)

  def update(): Unit = {
    if (activeBehavior == None && activeTask == None) {
      for (time ← bot.currentTime) {
        mode match {
          case BrainModes.JobMode if time.hours > 16 ⇒
            // Done with work?! Let's...uhm...EAT!
            mode = BrainModes.IdleMode
            for (i ← idle) executeBehavior(i)
          case BrainModes.NeedMode if time.hours > 8 ⇒
            // Switch to job if we don't have anything else to do. This ensures that we wait until the last SatisfactionBehavior is finished.
            mode = BrainModes.JobMode
            for (j ← job) activeBehavior = Some(j)
          case BrainModes.IdleMode if time.hours > 8 ⇒
            mode = BrainModes.JobMode
            for (j ← job) executeBehavior(j)
          case BrainModes.JobMode ⇒
            // If it's still time for work and the last job is done, we start over again.
            for (j ← job) executeBehavior(j)
          case BrainModes.NeedMode ⇒
            needManager.update()
            val suggestions = needManager.needSuggestion
            val strategy = needManager.getSatisfactionStrategyFor(suggestions.head)
            executeBehavior(strategy)
          case BrainModes.IdleMode ⇒
            needManager.update()
            for (i ← idle) executeBehavior(i)

        }
      }
    } else {
      for(b <- activeBehavior){
        if(b.isDone()){
          activeBehavior = None
        }
      }

      for(t <- activeTask){
        if(t.isDone()){
          activeTask = None
        }
      }
    }
  }
}