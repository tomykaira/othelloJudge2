package models

import java.io.File

/**
 * Client program runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:56 PM
 */

class Client(val path: File) {

  private var process: Process = null

  def run(port: Int): Process = {
    val builder = new ProcessBuilder(path.toString, "localhost", port.toString)
    builder redirectErrorStream false
    builder directory path.getParentFile

    process = builder.start()
    process
  }

  def destroy(): Unit = if (process != null) process.destroy
}
