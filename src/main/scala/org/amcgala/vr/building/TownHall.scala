package org.amcgala.vr.building

import akka.actor.{ActorLogging, Props, ActorRef}
import org.amcgala.vr.BuildingServicePackage.BuildingService
import BuildingService.JobDone
import org.amcgala.vr.Bot

object TownHall {

  trait TownHallMessage

  trait BuildingContract

  case object NoContract extends BuildingContract

  case class Contract(building: String, bot: Bot) extends BuildingContract

  case class RegisterBot(bot: Bot) extends TownHallMessage

  case object UnregisterBot extends TownHallMessage

  case class RegisterBuilding(building: String, bot: Bot) extends TownHallMessage

  case object RequestContract extends TownHallMessage

  case class RequestBuilding(building: String, bot: Bot) extends TownHallMessage

  def props() = Props(new TownHall)


  // TODO Weitere Nachrichten, die von dem Dorfzentrum bearbeitet werden sollen hier ergänzen.
}

class TownHall extends Building {

  import TownHall._

  var knownBots = Set[ActorRef]()
  var knownBuildings = Set[ActorRef]()
  var contracts = Set[Contract]()


  def taskHandling: Receive = {
    case RegisterBot      ⇒
    case UnregisterBot    ⇒
    case RegisterBuilding(building: String, bot: Bot) ⇒
      println("Registered!")

    case RequestContract =>
      if(contracts.isEmpty) {
        sender() ! Contract("No Contract", null)
      }
      else {
        val cont = contracts.head
        contracts -= cont
        sender() ! cont
      }

    case RequestBuilding(building, bot) =>
      contracts += new Contract(building, bot)
      println("added")
  }
}
