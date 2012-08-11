package models

import java.io.File
import play.Logger
import java.lang.Thread.UncaughtExceptionHandler

/**
 * Client program runner
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:56 PM
 */

class Client(val path: File) extends ProgramThread(new ProcessBuilder()) {

  def start(port: Int): Unit = {
    builder.command(path.toString, "localhost", port.toString)
    builder redirectErrorStream true
    builder directory path.getParentFile

    setUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable) = {
        Logger.error("Othello client error", e)
      }
    })

    start()
  }
}
