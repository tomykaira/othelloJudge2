package models

import java.io.{File}
import play.Logger
import java.lang.Thread.UncaughtExceptionHandler

/**
 * Server runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:57 PM
 */

class Server(val battle: Battle, val challenger: Client, val opponent: Client,
  val port: Int) {

  val ROUND_TIMEOUT = 60 * 1000
  val SERVER_PROGRAM = new File("/othello-judge/servers/reversi-serv")

  def run() = {
    val builder = new ProcessBuilder(SERVER_PROGRAM.toString,
      "-p", port.toString, "-t", ROUND_TIMEOUT.toString)
    builder redirectErrorStream true
    builder directory SERVER_PROGRAM.getParentFile

    val pt = new ProgramThread(builder)
    pt.setLineCallback { line =>
      if (line == "Waiting connections ... ") {
        println("invoking")
        challenger.start(port)
        Logger.info("challenger's client is started")
      }
      if (line == "One player is registered. Waiting for other player ...") {
        opponent.start(port)
        Logger.info("opponent's client is started")
      }
    }

    pt.setAfterCallback { exitValue =>
      challenger.forceExit
      opponent.forceExit

      if (exitValue == 0) {
        BattleRecorder.report(NormalExit(battle, pt.getOutput))
      } else {
        BattleRecorder.report(AbnormalExit(battle, pt.getOutput))
      }
    }

    pt.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable) = {
        Logger.error("Othello server error", e)
        BattleRecorder.report(AbnormalExit(battle, e.toString()))
      }
    })

    pt.start
  }
}
