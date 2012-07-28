package models

import java.io.{File, InputStreamReader, BufferedReader}

/**
 * Server runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:57 PM
 */

class Server(val battle: Battle, val port: Int)
  extends Thread {

  val GAME_TIMEOUT = 10000
  val SERVER_PROGRAM = new File("/tmp/sample/reversi-serv")

  override def run() = {
    val builder = new ProcessBuilder(SERVER_PROGRAM.toString,
      "-p", port.toString, "-t", "500")
    builder redirectErrorStream true
    builder directory SERVER_PROGRAM.getParentFile

    val proc = builder.start

    val streamReader = new InputStreamReader(proc.getInputStream)
    val bufferedReader = new BufferedReader(streamReader)
    val stringBuilder = new StringBuilder()
    var line:String = null
    while({line = bufferedReader.readLine; line != null}){
      stringBuilder.append(line)
      stringBuilder.append("\n")
      println("\t" + line)
    }
    bufferedReader.close

    val result = stringBuilder.toString

    proc.waitFor

    if (proc.exitValue == 0) {
      BattleRecorder.report(NormalExit(battle, result))
    } else {
      BattleRecorder.report(AbnormalExit(battle, result))
    }
  }
}
