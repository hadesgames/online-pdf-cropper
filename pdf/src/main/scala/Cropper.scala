package com.hadesgames.cropper;

import java.io.FileOutputStream

import com.itextpdf.text.{BaseColor, Rectangle, Document}
import com.itextpdf.text.pdf._
import com.sun.corba.se.impl.orbutil.closure.Future

import scala.sys.process.Process

/**
 * Created by hadesgames on 7/12/15.
 */

object Cropper extends App {

  def size(page: PdfDictionary): (Int, Int, Int, Int) = {
    val sizes = page.getAsArray(PdfName.MEDIABOX)
    val sx = sizes.getAsNumber(0).intValue()
    val sy = sizes.getAsNumber(1).intValue()
    val ex = sizes.getAsNumber(2).intValue()
    val ey = sizes.getAsNumber(3).intValue()

    return (sx, ex, sy, ey)
  }

  def enlarge(path: String, output: String): Unit = {
    val cmd = Seq(
      "/usr/bin/gs",
      "-o", output,
      "-sDEVICE=pdfwrite",
      "-g30600x39600",
      "-dPDFFitPage",
      "-dUseCropBox",
      path)

    println(cmd)
    println(Process(cmd).!!)
  }


  def addCropPaged(copy: PdfCopy, reader: PdfReader, i: Int, media: PdfRectangle, crop: PdfRectangle): Unit = {
    val page = reader.getPageN(i)

    page.put(PdfName.MEDIABOX, media)
    page.put(PdfName.CROPBOX, crop)

    val iPage = copy.getImportedPage(reader, i)
    copy.addPage(iPage)
  }

  def crop(path: String, output: String, xeps: Int = 50, yeps: Int = 0): Unit = {
    val readers = (new PdfReader(path), new PdfReader(path), new PdfReader(path), new PdfReader(path))
    val doc = new Document()
    val copy = new PdfCopy(doc, new FileOutputStream(output))
    doc.open()

    val n = readers._1.getNumberOfPages()

    for { i <- 1 to n} {
      val page = readers._1.getPageN(i)

      val sizes = page.getAsArray(PdfName.MEDIABOX)
      val sx = sizes.getAsNumber(0).intValue()
      val sy = sizes.getAsNumber(1).intValue()
      val ex = sizes.getAsNumber(2).intValue()
      val ey = sizes.getAsNumber(3).intValue()


      val cropbox = new PdfRectangle(0, 0, ex , ey)

      val mediabox = (
        new PdfRectangle(0, ey/2 - xeps,  ex / 2 + yeps, ey),
        new PdfRectangle(0, 0, ex / 2 + yeps, ey / 2 + xeps),
        new PdfRectangle(ex/2 - yeps, ey/2 - xeps, ex, ey),
        new PdfRectangle(ex/2 - yeps, 0, ex, ey / 2 + xeps))

      addCropPaged(copy, readers._1, i, mediabox._1, cropbox)
      addCropPaged(copy, readers._2, i, mediabox._2, cropbox)
      addCropPaged(copy, readers._3, i, mediabox._3, cropbox)
      addCropPaged(copy, readers._4, i, mediabox._4, cropbox)
    }
    /*
    val page = reader.getPageN(1)

    val sizes = page.getAsArray(PdfName.MEDIABOX)
    val sx = sizes.getAsNumber(0).intValue()
    val sy = sizes.getAsNumber(1).intValue()
    val ex = sizes.getAsNumber(2).intValue()
    val ey = sizes.getAsNumber(3).intValue()

    print(sx, ex, sy, ey)

    val rect = new PdfRectangle(0, 0, ex / 2, ey / 2)
    val rect2 = new PdfRectangle(0, 0, ex, ey)

    print(sx, ex, sy, ey)
    page.put(PdfName.MEDIABOX, rect)
    page.put(PdfName.CROPBOX, rect2)
    val stamper = new PdfStamper(doc, new FileOutputStream(output))
    stamper.close()*/
    /*PdfWriter.getInstance(doc, new FileOutputStream(output)).close();*/
    doc.close()
    copy.close()
    readers._1.close()
    readers._2.close()
    readers._3.close()
    readers._4.close()
  }

 // enlarge("/tmp/test.pdf", "/tmp/enlarge.pdf")
 // crop("/tmp/enlarge.pdf", "/tmp/output")
/*  val page = reader.getPageN(1)

  val sizes = page.getAsArray(PdfName.MEDIABOX)
  val sx = sizes.getAsNumber(0).intValue()
  val sy = sizes.getAsNumber(1).intValue()
  val ex = sizes.getAsNumber(2).intValue()
  val ey = sizes.getAsNumber(3).intValue()

  print(sx, ex, sy, ey)

  val rect = new PdfRectangle(0, 0, ex / 2, ey / 2)
  val rect2 = new PdfRectangle(0, 0, ex, ey)

  print(sx, ex, sy, ey)
  page.put(PdfName.MEDIABOX, rect)
  page.put(PdfName.CROPBOX, rect2)
  val stamper = new PdfStamper(reader, new FileOutputStream("/tmp/out.pdf"))
  stamper.close()
  reader.close()*/
}
