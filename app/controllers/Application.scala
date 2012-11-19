package controllers

import play.api.mvc._
import models.Submissions
import java.nio.charset.Charset
import play.core.parsers.FormUrlEncodedParser
import play.api.Logger

/**
 * Web application controller, whose main purpose is to handle HTTP POST requests.
 * The index page is for testing, and to view the journal.
 */
object Application extends Controller {

  val MaxBodyLengthBytes = 1024

  /**
   * Handle a POST request by writing the raw request body to persistent storage.
   *
   * @param redirectUrl Optional URL to redirect to after handling the submission
   */
  def submit(redirectUrl: Option[String]) = Action(parse.raw) { implicit request =>
    request.body.asBytes(MaxBodyLengthBytes).map { rawRequest =>
      Logger.debug(new String(rawRequest, "UTF-8"))
      Submissions save rawRequest
    }
    redirectUrl.map{url =>
      Redirect(url)
    }.getOrElse{
      Redirect(routes.Application.index())
    }
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