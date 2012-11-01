package models

import journal.io.api.Journal
import journal.io.api.Journal.{ReadType, WriteType}
import play.api.{Logger, Play}
import play.api.Play.current
import scala.Predef.String
import java.nio.charset.Charset

/**
 * Journal.IO file-based persistence, using a singleton thread safe journal instance,
 * which is initialised and opened on first use.
 */
object SubmissionJournal {

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
  def write(record: Array[Byte]) {
    journal.write(record, WriteType.SYNC)
    Logger.info("WRITE " + new String(record, Charset.forName("UTF-8")))
  }

  /**
   * Read journal entries, most recent first.
   */
  def recent(): Iterable[Array[Byte]] = {
    import scala.collection.JavaConversions._
    journal.undo().map { location =>
      journal.read(location, ReadType.SYNC);
    }
  }
}
