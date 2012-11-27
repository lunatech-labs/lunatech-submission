package models

import journal.io.api.Journal
import journal.io.api.Journal.{ReadType, WriteType}
import play.api.Play
import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import java.io.ByteArrayOutputStream

/**
 * Journal.IO file-based persistence, using a singleton thread safe journal instance,
 * which is initialised and opened on first use.
 */
object Submissions {

  val JournalDirectory = "data"
  val RecentPageSize = 20
  val MinChunkSizeBytes = 8 * 1024

  val journal = new Journal()
  journal.setDirectory(Play getFile JournalDirectory)
  journal.open()

  def close() {
    journal.close()
  }

  /**
   * Write the given byte array directly to the journal.
   */
  def save(record: Array[Byte]) {
    journal.write(record, WriteType.SYNC)
  }

  /**
   * Read journal entries, most recent first.
   */
  def recent: Iterable[Array[Byte]] = {
    import scala.collection.JavaConversions._
    journal.undo.take(RecentPageSize) map { location =>
      journal.read(location, ReadType.SYNC)
    }
  }

  /**
   * An Enumerator that reads all journal entries, used for streaming output.
   * Read multiple records per chunk to improve performance.
   */
  def all:Enumerator[Array[Byte]]  = {
    val submissions = journal.redo().iterator()

    Enumerator.fromCallback[Array[Byte]] (() => {
      val submission = if (submissions.hasNext) {
        val buffer = new ByteArrayOutputStream()
        var bufferSize = 0
        while (submissions.hasNext && bufferSize < MinChunkSizeBytes) {
          val location = submissions.next()
          val record = journal.read(location, ReadType.ASYNC)
          buffer.write(record)
          bufferSize = bufferSize + record.length
        }
        Some(buffer.toByteArray)
      } else {
        None
      }
      Promise.pure(submission)
    })
  }

}
