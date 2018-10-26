package controllers

import java.net.URL
import java.util.Scanner

import javax.inject.Inject
import models.URLModel
import play.api.data._
import play.api.mvc._

class HomeController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import URLForm._

  private var url: URLModel = _

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.HomeController.createURL()

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(url, form, postUrl))
  }

  // This will be the action that handles our form post
  def createURL = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[URLData] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.index(url, formWithErrors, postUrl))
    }

    val successFunction = { data: URLData =>
      // This is the good case, where the form was successfully parsed as a Data object.
      val myUrl = URLModel(url = data.url, title = "")

      try {
        val inputStream = new URL(data.url).openStream()
        val scanner = new Scanner(inputStream)
        val responseBody = scanner.useDelimiter("\\A").next()
        myUrl.title = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"))
      } catch {
        case nse: NoSuchElementException => nse.printStackTrace()
      }

      url = myUrl

      Ok(views.html.index(url, form, postUrl))
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

}

