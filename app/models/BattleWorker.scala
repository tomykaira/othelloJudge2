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

        (black, white) match {
          case (Right(b), Right(w)) =>
            val server = new Server(m, b, w, port)
            server.run()
          case (Left(mb), Right(_)) =>
            BattleRecorder.report(AbnormalExit(m, "client not created"), mb, "")
          case (Right(_), Left(mw)) =>
            BattleRecorder.report(AbnormalExit(m, "client not created"), "", mw)
          case (Left(mb), Left(mw)) =>
            BattleRecorder.report(AbnormalExit(m, "client not created"), mb, mw)
        }
      }
    }
  }

  private def compiledClient(mail: String, version: Int): Either[String, Client] =
    Program.find(mail, version).toRight("program not found in DB")
      .fold(l => Left(l), r => r.prepare())
      .fold(l => Left(l), r => Right(new Client(r)))
}
