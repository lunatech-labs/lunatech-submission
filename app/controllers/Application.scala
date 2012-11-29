package controllers

import play.api.mvc._
import models.Submissions
import java.nio.charset.Charset
import play.core.parsers.FormUrlEncodedParser
import play.api.Logger
import play.api.libs.iteratee.{Enumerator, Enumeratee}
import play.api.libs.json.Json

/**
 * Web application controller, whose main purpose is to handle HTTP POST requests.
 * The index page is for testing, and to view the journal.
 */
object Application extends Controller {

  val MaxBodyLengthBytes = 10 * 1024

  /**
   * Handle a POST request by writing the raw request body to persistent storage.
   *
   * @param redirectUrl Optional URL to redirect to after handling the submission
   */
  def submit(redirectUrl: Option[String]) = Action(parse.raw) { implicit request =>
    request.body.asBytes(MaxBodyLengthBytes) map { rawRequest =>
      Logger.debug(new String(rawRequest, "UTF-8"))
      Submissions.save(rawRequest)
    } getOrElse {
      Logger.error("Request body bigger than max length: %d bytes" format MaxBodyLengthBytes)
    }

    redirectUrl map { url =>
      Redirect(url)
    } getOrElse {
      Redirect(routes.Application.index)
    }
  }

  /**
   * Display an index page with a recent of form submissions.
   */
  def index = Action {
    Ok(views.html.index())
  }

  /**
   * Stream all stored submissions in JSON format, with one JSON object per line.
   *
   * It would be nicer to output a well-formed JSON array, but Play Framework 2.0 bug #666 prevents adding a final ']'.
   */
  def export = Action {
    val headers = "Content-Disposition" -> "attachment; filename=export.json"
    Ok.stream(Enumerator("[") >>> Submissions.json).withHeaders(headers)
  }


  /**
   * Display a page with a table of recent form submissions.
   */
  def recent = Action {
    val submissions = Submissions.recent map { Submissions.parseFormData(_) }
    val columns = submissions.flatMap(_.keys).toSet.toList
    Ok(views.html.recent(submissions, columns))
  }

}