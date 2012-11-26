package models

import journal.io.api.Journal
import journal.io.api.Journal.{ReadType, WriteType}
import play.api.Play
import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import java.io.InputStream
import play.api.libs.concurrent.Promise

/**
 * Journal.IO file-based persistence, using a singleton thread safe journal instance,
 * which is initialised and opened on first use.
 */
object Submissions {

  val JournalDirectory = "data"
  val RecentPageSize = 20

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
   */
  def all:Enumerator[Array[Byte]]  = {
    val submissions = journal.redo().iterator()

    Enumerator.fromCallback[Array[Byte]] (() => {
      val submission = if (submissions.hasNext) {
        val location = submissions.next()
        Some(journal.read(location, ReadType.SYNC))
      } else {
        None
      }
      Promise.pure(submission)
    })
  }

}
