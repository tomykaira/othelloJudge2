package models

import scala.actors.Actor
import Actor._
import play.Logger

object BattleWorker extends Actor {
  def act = loop {
    react {
      case m: Battle => {

        val challenger = compiledClient(m.challengerMail, m.challengerVersion)
        val opponent = compiledClient(m.opponentMail, m.opponentVersion)

        val port = System.currentTimeMillis.toInt % 1000 + 30000

        Logger.info("matchmaking " + m.shortInfo)
        if (challenger.isDefined && opponent.isDefined) {

          val server = new Server(m, challenger.get, opponent.get, port)
          server.run()
        } else {
          BattleRecorder.report(AbnormalExit(m, "client not created"))
        }
      }
    }
  }

  private def compiledClient(mail: String, version: Int): Option[Client] =
    Program.find(mail, version).flatMap(_.prepare()).map(new Client(_))
}
