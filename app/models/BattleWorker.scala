package models

import scala.actors.Actor
import Actor._
import play.Logger

object BattleWorker extends Actor {
  def act = loop {
    react {
      case m: Battle => {

        val black = compiledClient(m.blackMail, m.blackVersion)
        val white = compiledClient(m.whiteMail, m.whiteVersion)

        val port = System.currentTimeMillis.toInt % 1000 + 30000

        Logger.info("matchmaking " + m.shortInfo)
        if (black.isDefined && white.isDefined) {

          val server = new Server(m, black.get, white.get, port)
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
