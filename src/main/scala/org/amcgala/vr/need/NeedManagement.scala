package org.amcgala.vr.need

import org.amcgala.vr.Behavior
import org.amcgala.vr.need.Needs.NeedIDs.{ HungerID, NeedID }

trait SatisfactionBehavior extends Behavior {
  val need: Need
}

trait Need {
  val id: NeedID
  var needManager: Option[NeedManager] = None

  var value: Double

  def addSatisfactionBehavior(behavior: SatisfactionBehavior): Unit

  def satisfactionBehaviors: List[SatisfactionBehavior]

  def increase(): Double

  def decrease(): Double

  def increase(delta: Double): Double

  def decrease(delta: Double): Double

  def update(): Unit
}

object Needs {

  object NeedIDs {
    sealed trait NeedID
    case object HungerID extends NeedID
    case object InfectionID extends NeedID
  }

  class Hunger extends Need
  {

    val id = NeedIDs.HungerID

    var manager: Option[NeedManager] = None

    var value: Double = 100

    var behaviors: List[SatisfactionBehavior] = List.empty[SatisfactionBehavior]

    def addSatisfactionBehavior(behavior: SatisfactionBehavior): Unit =
    {
      behaviors = behavior :: behaviors
    }

    def satisfactionBehaviors: List[SatisfactionBehavior] =
    {
      return behaviors
    }

    def decrease(): Double =
    {
      value -= 0.10

      return value
    }

    def decrease(delta: Double): Double =
    {
      value -= delta

      return value
    }

    def increase(): Double =
    {
      value += 0.10

      return value
    }

    def increase(delta: Double): Double =
    {
      value += delta

      return value
    }

    def update(): Unit =
    {
      this.decrease()
    }
  }



  object Hunger {
    def apply(): Hunger = new Hunger()

    val ID = NeedIDs.HungerID
  }

}

class NeedManager {
  private var needList = List.empty[Need]

  def removeNeed(id: NeedID): Unit = {
    needList = needList.filterNot(_.id == id)
  }

  def getSatisfactionStrategyFor(need: Need): SatisfactionBehavior = {
    // TODO Gibt ein Verhalten zurück, das vom Brain ausgeführt werden soll.
    null
  }

  def needSuggestion: List[Need] = {
    // TODO Zusammenstellen einer Liste von Bedürfnissen, die befriedigt werden müssen.
    null
  }

  def registerNeed(need: Need): Unit = {
    // Zur Liste hinzufügen
    need.needManager = Some(this)
    // TODO Hinzufügen eines neues Bedürfnis zu der Liste aller Bedürfnisse
  }

  def update(): Unit = {
    // TODO Aktualisieren aller Needs, die vom Manager verwaltet werden
  }
}
