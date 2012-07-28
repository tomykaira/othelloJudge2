package models

/**
 * Hand made simple zip handler, and some
 * User: tomykaira
 * Date: 7/28/12
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io._
import scala.io._
import java.util.zip.{ZipEntry, ZipFile}
import java.util.Enumeration
import org.apache.commons.io.FileUtils

// from http://harrah.github.com/browse/samples/sbt/sbt/FileUtilities.scala.html
object FileUtilities {
  class RichEnumeration[T](enumeration:Enumeration[T]) extends Iterator[T] {
    def hasNext:Boolean =  enumeration.hasMoreElements()
    def next:T = enumeration.nextElement()
  }

  implicit def enumerationToRichEnumeration[T](enumeration:Enumeration[T]):RichEnumeration[T] = {
    new RichEnumeration(enumeration)
  }

  def unzipTo(zip: File, root: File): Option[String] = {
    try {
      createDirectory(root)
      val zipInput = new ZipFile(zip)
      extract(zipInput, root)
      None
    } catch {
      case e: Exception => Some("unzip error: " + e)
    }
  }

  def createDirectory(path: File): Unit = {
    if(! path.exists)
      path.mkdirs
  }

  private def extract(from: ZipFile, toDir: File) = {
    from.entries.foreach { entry =>
      val target = new File(toDir, entry.getName)
      if (entry.isDirectory)
        createDirectory(target)
      else
        FileUtils.copyInputStreamToFile(from.getInputStream(entry), target)
    }
  }

  def deleteAll(file: File) : Unit = {
    def deleteFile(file : File) : Unit = {
      if(file.isDirectory)
        file.listFiles.foreach{ f => deleteFile(f) }
      file.delete
    }
    deleteFile(file)
  }
}
