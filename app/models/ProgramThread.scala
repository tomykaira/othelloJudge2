package models

import java.io.{InputStreamReader, BufferedReader}

class ProgramThread(builder: ProcessBuilder) extends Thread {
  private var process: Process = null
  private val stringBuilder = new StringBuilder()
  private var callback: Option[String => Unit] = None

  def setCallback(acb: String => Unit) = callback = Some(acb)

  override def run() = {
    process = builder.start
    val streamReader = new InputStreamReader(process.getInputStream)
    val bufferedReader = new BufferedReader(streamReader, 1)
    var line:String = null

    while({line = bufferedReader.readLine; line != null}){
      stringBuilder.append(line)
      stringBuilder.append("\n")
      callback.map(_(line))
    }
    bufferedReader.close

    process.waitFor
  }

  def waitFor: Unit = if (process != null) process.waitFor

  def forceExit: Unit = if (process != null) process.destroy

  def getOutput: String = stringBuilder.toString

  def exitValue: Int = process.exitValue
}
