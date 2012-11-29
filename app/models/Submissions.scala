package models

import journal.io.api.Journal
import journal.io.api.Journal.{ReadType, WriteType}
import play.api.{Logger, Play}
import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import play.api.libs.json.Json
import play.core.parsers.FormUrlEncodedParser
import java.nio.charset.Charset

/**
 * Journal.IO file-based persistence, using a singleton thread safe journal instance,
 * which is initialised and opened on first use.
 */
object Submissions {

  val JournalDirectory = "data"
  val RecentPageSize = 20

  // An 8KB HTTP response chunk size is Play’s default for streaming Enumerators.
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
      journal.read(location, ReadType.ASYNC)
    }
  }

  /**
   * Parse a single submission as UTF-8 form-encoded text.
   */
  def parseFormData(formData: Array[Byte]): Map[String,Seq[String]] = {
    FormUrlEncodedParser.parse(new String(formData, Charset.forName("UTF-8")))
  }

  /**
   * An Enumerator that reads all journal entries, used for streaming output. Read multiple records (in the while loop)
   * per chunk to improve performance. The form data parsing and JSON formatting could be done in Enumeratees in the
   * controller, but that doesn't perform so well.
   */
  def json:Enumerator[String]  = {
    import scala.collection.JavaConverters._
    val submissions = journal.redo().iterator.asScala

    Enumerator.fromCallback[String] (() => {
      val submission = if (submissions.hasNext) {
        val buffer = new StringBuilder()
        while (submissions.hasNext && buffer.length < MinChunkSizeBytes) {
          val location = submissions.next
          val record = journal.read(location, ReadType.ASYNC)
          val json = Json.toJson(parseFormData(record))
          buffer.append(json.toString)

          // Closing the JSON array here is a work-around for Lighthouse ticket:
          // #666 Enumerator 'andThen' method doesn't remove EOF from left enumerator
          // The array is still opened in the controller because it's ugly to do it here.
          buffer.append(if (submissions.hasNext) "," else "]")
        }
        Some(buffer.toString)
      } else {
        None
      }
      Promise.pure(submission)
    })
  }

}
