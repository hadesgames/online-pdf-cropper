package controllers

import java.io.File

import com.hadesgames.cropper.Cropper
import play.api._
import play.api.mvc._

import scala.util.Random

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upload = Action(parse.multipartFormData) { request =>
    val xexp = request.body.dataParts("x_offset").headOption.map(_.toInt).getOrElse(0)
    val yexp = request.body.dataParts("y_offset").headOption.map(_.toInt).getOrElse(0)

    request.body.file("pdf").map { pdf =>
      val seed = Random.nextInt()
      val initial = s"/tmp/$seed.pdf"
      val enlarged = s"/tmp/enlarged-$seed.pdf"
      val cropped = s"/tmp/cropped-$seed.pdf"

      pdf.ref.moveTo(new File(s"/tmp/$seed.pdf"))
      Cropper.enlarge(initial, enlarged)
      Cropper.crop(enlarged, cropped, xexp, yexp)
      Ok.sendFile(new File(cropped), fileName = _ => "cropped-" + pdf.filename)
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }

  }

}
