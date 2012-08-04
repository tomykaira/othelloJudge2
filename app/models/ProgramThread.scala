package models

import java.io.{InputStreamReader, BufferedReader}

class ProgramThread(protected val builder: ProcessBuilder) extends Thread {
  private var process: Process = null
  private val stringBuilder = new StringBuilder()
  private var lineCallback: Option[String => Unit] = None
  private var afterCallback: Option[Int => Unit] = None

  def setLineCallback(acb: String => Unit) = lineCallback = Some(acb)

  def setAfterCallback(acb: Int => Unit) = afterCallback = Some(acb)

  override def run() = {
    process = builder.start
    val streamReader = new InputStreamReader(process.getInputStream)
    val bufferedReader = new BufferedReader(streamReader, 1)
    var line:String = null

    while({line = bufferedReader.readLine; line != null}){
      stringBuilder.append(line)
      stringBuilder.append("\n")
      lineCallback.map(_(line))
    }
    bufferedReader.close

    process.waitFor

    afterCallback.map(_(exitValue))
  }

  def waitFor: Unit = if (process != null) process.waitFor

  def forceExit: Unit = if (process != null) process.destroy

  def getOutput: String = stringBuilder.toString

  def exitValue: Int = process.exitValue
}
