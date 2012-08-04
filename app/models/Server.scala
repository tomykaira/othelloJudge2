package models

import java.io.{File}
import play.Logger

/**
 * Server runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:57 PM
 */

class Server(val battle: Battle, val challenger: Client, val opponent: Client,
  val port: Int)
extends Thread {

  val ROUND_TIMEOUT = 60 * 1000
  val SERVER_PROGRAM = new File("/othello-judge/servers/reversi-serv")

  override def run() = {
    val builder = new ProcessBuilder(SERVER_PROGRAM.toString,
      "-p", port.toString, "-t", ROUND_TIMEOUT.toString)
    builder redirectErrorStream true
    builder directory SERVER_PROGRAM.getParentFile

    try {
      val pt = new ProgramThread(builder)
      pt.setCallback { line =>
        if (line == "Waiting connections ... ") {
          println("invoking")
          challenger.run(port)
          Logger.info("challenger's client is started")
        }
        if (line == "One player is registered. Waiting for other player ...") {
          opponent.run(port)
          Logger.info("opponent's client is started")
        }
      }

      pt.start
      pt.join

      challenger.destroy()
      opponent.destroy()

      if (pt.exitValue == 0) {
        BattleRecorder.report(NormalExit(battle, pt.getOutput))
      } else {
        BattleRecorder.report(AbnormalExit(battle, pt.getOutput))
      }
    } catch {
      case e: Exception =>
        Logger.error("Othello server error", e)
        BattleRecorder.report(AbnormalExit(battle, e.toString()))
    }

  }
}
