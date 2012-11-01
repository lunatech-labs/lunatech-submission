import models.SubmissionJournal
import play.api.GlobalSettings

/**
 * Application life-cycle handler, to close the journal on shutdown.
 */
object Global extends GlobalSettings {

  override def onStop(app: play.api.Application) {
    SubmissionJournal.close
  }
}
