package models

import java.io.{File, InputStreamReader, BufferedReader}
import play.Logger

/**
 * Server runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:57 PM
 */

class Server(val battle: Battle, val port: Int)
extends Thread {

  val ROUND_TIMEOUT = 2000
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
      val bufferedReader = new BufferedReader(streamReader)
      var line:String = null

      while({line = bufferedReader.readLine; line != null}){
        stringBuilder.append(line)
        stringBuilder.append("\n")
      }
      bufferedReader.close

      val result = stringBuilder.toString

      proc.waitFor

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
