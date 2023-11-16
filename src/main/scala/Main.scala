import zio.*
import zio.Console.printLine
import zio.http.*
import zio.json.*

import scodec.*
import scodec.bits.*
import scodec.codecs.*

import mp3.*



val header: Codec[(Unit, Int, Int, Boolean, Boolean, Boolean, Boolean, Unit, Int, Int, Int, Int)] =
  ("ID3 Identifier" | constant(utf8.encode("ID3").require)) ::
    ("ID3v2 Version" | uint8) ::
    ("ID3v2 Subversion" | uint8) ::
    bool ::
    bool ::
    bool ::
    bool ::
    ("Flag Suffix" | constant(bin"0000")) ::
    uint8 ::
    uint8 ::
    uint8 ::
    uint8

val extractSynchSafe = (_: Int) << 21 | (_: Int) << 14 | (_: Int) << 7 | (_: Int)

def handleAnalyze(req: Request): IO[Response, Response] = {
  if (req.header(Header.ContentType).exists(_.mediaType == MediaType.multipart.`form-data`))
    for {
      form <- req.body.asMultipartForm.mapError(e => Response(Status.InternalServerError, body=Body.fromString(e.getMessage)))
      response <- form.get("file") match {
        case Some(file) => file match
          case FormField.Binary(name, data, contentType, transferEncoding, filename) => {
            for {
              byteArray <- ZIO.attempt(data.toArray)
              fileSize <- ZIO.succeed(byteArray.length.toDouble / (1024.0 * 1024.0))
              bitVector <- ZIO.attempt(ByteVector(byteArray).bits)
              id3Header <- ZIO.attempt(header.decode(bitVector.slice(0, 80)))
              audioStart <- ZIO.attempt(id3Header.map(a => extractSynchSafe(a.value._9, a.value._10, a.value._11, a.value._12) * 8).require)
              start <- ZIO.attempt(bitVector.indexOfSlice(bin"1111 1111 1111", audioStart))
              frameHeader <- ZIO.attempt(FrameHeaderCodec().decode(bitVector.slice(start, start+32)).require.value)
            } yield Response.json(frameHeader.toJson)
          }.mapError(e => Response(Status.InternalServerError, body=Body.fromString(e.getMessage)))
        case _ => ZIO.fail(Response(Status.BadRequest, body = Body.fromString("File has to be binary")))

        case None => ZIO.fail(Response(Status.BadRequest, body = Body.fromString("No file found")))
      }
    } yield response
  else ZIO.succeed(Response(status = Status.NotFound))
}


object Main extends ZIOAppDefault:
  val app: HttpApp[Any] =
    Routes(
      Method.POST / "analyze" -> handler((req: Request) => handleAnalyze(req).logError("Hi"))
  ,
  Method.GET / "alive" -> handler(Response.ok)
  ).toHttpApp

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Any, Any] =
    Server.serve(app).provide(Server.defaultWith(_ => Server.Config.default.port(8000).requestStreaming(Server.RequestStreaming.Enabled)))