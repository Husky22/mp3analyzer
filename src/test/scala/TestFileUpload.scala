import java.io.File
import zio.*
import zio.test.*
import zio.test.ZIOSpecDefault
import zio.http.*
import scodec.*
import scodec.bits.*
import scodec.codecs.*
import zio.Console.printLine

val header: Codec[(Unit, Int, Int, Boolean, Boolean, Boolean, Boolean, Unit, Int, Int, Int, Int)] =
  ("ID3 Identifier" | constant(utf8.encode("ID3").require))::
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

def handleAnalyze(req: Request) = {
  for {
    bodyStr <- req.body.asString
    _ <- printLine(bodyStr.take(100))
    byteChunk<- req.body.asChunk
    byteArray <- ZIO.attempt(byteChunk.toArray).debug("Array")
    _ <- ZIO.succeed(byteArray.length.toDouble / (1024.0 * 1024.0)).debug("MBs")
    id3Header <- ZIO.succeed(header.decode(ByteVector(byteArray).bits.slice(0, 80)))
    audioStart <- ZIO.attempt(id3Header.map(a => extractSynchSafe(a.value._9, a.value._10, a.value._11, a.value._12) * 8).require)
  } yield Response.text(s"Audio Start position: ${audioStart}")
}

object TestFileUpload extends ZIOSpecDefault{
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("http") (
    test("upload file") {
      val app = handler( (req: Request) => handleAnalyze(req).logError("Analyze").orDie).toHttpApp
      val req = Request.post(path="/", body = Body.fromFile(File("/Users/nvm/Documents/Programmieren/Scala/SoundLibManager/assets/TestMP3/&Me - Woods.mp3")))
      assertZIO(app.runZIO(req))(Assertion.equalTo(Response.text("Audio Start position: 808968")))
    }
  )

}
