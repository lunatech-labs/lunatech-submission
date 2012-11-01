package controllers

import play.api.mvc._
import models.Submissions
import java.nio.charset.Charset
import play.core.parsers.FormUrlEncodedParser

/**
 * Web application controller, whose main purpose is to handle HTTP POST requests.
 * The index page is for testing, and to view the journal.
 */
object Application extends Controller {

  val MaxBodyLengthBytes = 1024

  /**
   * Handle a POST request by writing the raw request body to persistent storage.
   * TODO: add a way for the request to specify the redirect URL.
   */
  def submit = Action(parse.raw) { implicit request =>
    request.body.asBytes(MaxBodyLengthBytes).map { rawRequest =>
      Submissions save rawRequest
    }
    Redirect(routes.Application.index())
  }

  /**
   * Display an index page with a list of form submissions.
   */
  def index = Action {
    val submissions = load
    val columns = submissions.flatMap(_.keys).toSet.toList
    Ok(views.html.index(submissions, columns))
  }

  /**
   * Read previous submissions from storage, parsing them as UTF-8 form-encoded text.
   */
  private def load(): Iterable[Map[String,Seq[String]]] = {
    Submissions.list.map { record =>
      FormUrlEncodedParser.parse(new String(record, Charset.forName("UTF-8")))
    }
  }

}