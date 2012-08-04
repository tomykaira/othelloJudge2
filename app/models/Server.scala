package models

import java.io.{File, InputStreamReader, BufferedReader}
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
    val stringBuilder = new StringBuilder()
    builder redirectErrorStream true
    builder directory SERVER_PROGRAM.getParentFile

    try {
      val proc = builder.start
      val streamReader = new InputStreamReader(proc.getInputStream)
      val bufferedReader = new BufferedReader(streamReader, 1)
      var line:String = null
      var char: Int = 0

      challenger.run(port)

      while({line = bufferedReader.readLine; line != null}){
        stringBuilder.append(line)
        stringBuilder.append("\n")
        if (line == "One player is registered. Waiting for other player ...") {
          opponent.run(port)
          Logger.info("opponent's client is started")
        }
      }
      bufferedReader.close

      val result = stringBuilder.toString

      proc.waitFor

      challenger.destroy()
      opponent.destroy()

      if (proc.exitValue == 0) {
        BattleRecorder.report(NormalExit(battle, result))
      } else {
        BattleRecorder.report(AbnormalExit(battle, result))
      }
    } catch {
      case e: Exception =>
        Logger.error("Othello server error", e)
        BattleRecorder.report(AbnormalExit(battle, e.toString()))
    }

  }
}
