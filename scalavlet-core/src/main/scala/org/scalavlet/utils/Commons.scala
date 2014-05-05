package org.scalavlet.utils

import java.io._
import scala.annotation.tailrec
import java.nio.channels.Channels

// This is added based on the advise of compiler warning
import scala.language.reflectiveCalls

object Commons {

  /**
   * Executes a block with a closeable resource, and closes it after the block runs
   *
   * @tparam A the return type of the block
   * @tparam B the closeable resource type
   * @param closeable the closeable resource
   * @param f the block
   */
  def using[A, B <: { def close(): Unit }](closeable: B)(f: B => A) {
    try {
      f(closeable)
    }
    finally {
      if (closeable != null)
        closeable.close()
    }
  }


  /**
   * Copies the input stream to the output stream.
   *
   * @param in the input stream to read.  The InputStream will be closed, unlike
   * commons-io's version.
   * @param out the output stream to write
   * @param bufferSize the size of buffer to use for each read
   */
  def copy(in: InputStream, out: OutputStream, bufferSize: Int = 4096) {
    using(in) { in =>
      val buf = new Array[Byte](bufferSize)
      @tailrec
      def loop() {
        val n = in.read(buf)
        if (n >= 0) {
          out.write(buf, 0, n)
          loop()
        }
      }
      loop()
    }
  }


  /**
   *
   */
  def zeroCopy(in: FileInputStream, out: OutputStream) {
    using(in.getChannel) { ch => ch.transferTo(0, ch.size, Channels.newChannel(out)) }
  }


  /**
   *
   */
  def readBytes(in: InputStream): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    copy(in, out)

    out.toByteArray
  }

  /**
   * Creates a temp file, passes it to a block, and removes the temp file on the block's completion.
   *
   * @tparam A the return type of the block
   * @param content The content of the file
   * @param prefix The prefix of the temp file; must be at least three characters long
   * @param suffix The suffix of the temp file
   * @param directory The directory of the temp file; a system dependent temp directory if None
   * @param f the block
   * @return the result of f
   */
  def withTempFile[A](content: String, prefix: String = "scalavlet", suffix: String = ".tmp",  directory: Option[File] = None)(f: File => A) = {
    val tmp = File.createTempFile(prefix, suffix, directory.getOrElse(null))
    try {
      using(new BufferedWriter(new FileWriter(tmp))) { out =>
        out.write(content)
        out.flush()
      }
      f(tmp)
    }
    finally {
      tmp.delete()
    }
  }
}
