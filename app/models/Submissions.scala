package models

import journal.io.api.Journal
import journal.io.api.Journal.{ReadType, WriteType}
import play.api.Play
import play.api.Play.current

/**
 * Journal.IO file-based persistence, using a singleton thread safe journal instance,
 * which is initialised and opened on first use.
 */
object Submissions {

  val JOURNAL_DIRECTORY = "data"

  val journal = new Journal()
  journal.setDirectory(Play getFile JOURNAL_DIRECTORY)
  journal.open

  def close {
    journal.close
  }

  /**
   * Write the given byte array directly to the journal.
   */
  def save(record: Array[Byte]) {
    journal.write(record, WriteType.SYNC)
  }

  /**
   * Read journal entries, most list first.
   */
  def list(): Iterable[Array[Byte]] = {
    import scala.collection.JavaConversions._
    journal.undo().map { location =>
      journal.read(location, ReadType.SYNC);
    }
  }
}
