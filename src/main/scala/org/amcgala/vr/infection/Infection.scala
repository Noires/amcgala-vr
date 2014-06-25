package org.amcgala.vr.infection

import org.amcgala.vr.need.{NeedManager, SatisfactionBehavior, Need}
import org.amcgala.vr.need.Needs.NeedIDs.InfectionID
import org.amcgala.vr.need.Needs.NeedIDs

class Infection extends Need
{

  val id = NeedIDs.InfectionID

  var manager: Option[NeedManager] = None

  var value: Double = 1

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
    // Do nothing. Be sick.
    return value
  }

  def decrease(delta: Double): Double =
  {
    // Do nothing. Be sick.
    return value
  }

  def increase(): Double =
  {
    // Do nothing. Be sick.
    return value
  }

  def increase(delta: Double): Double =
  {
    // Do nothing. Be sick.
    return value
  }

  def update(): Unit =
  {
    // Do nothing. Be sick.
  }
}

object Infection {
  def apply(): Infection = new Infection()

  val ID = NeedIDs.InfectionID
}

