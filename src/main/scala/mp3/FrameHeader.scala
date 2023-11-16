package mp3

import mp3.Version.{V1, V2, V2_5}
import scodec.*
import scodec.codecs.*
import scodec.bits.*

import zio.json.*

enum Version(val num: Int):
  case V2_5 extends Version(0)
  case Reserved extends Version(1)
  case V2 extends Version(2)
  case V1 extends Version(3)

object Version {
  def apply(i: Int) =
    Version.values.find(_.num == i).toRight("Version is Invalid")

  implicit val encoder: JsonEncoder[Version] = JsonEncoder[String].contramap(_.toString)
}

enum Layer(val num: Int):
  case III extends Layer(1)
  case II extends Layer(2)
  case I extends Layer(3)

object Layer {
  def apply(i: Int) =
    Layer.values.find(_.num == i).toRight("Layer is Invalid")

  implicit val encoder: JsonEncoder[Layer] = JsonEncoder[String].contramap(_.toString)
}

enum V1L3Bitrate(val num: Int):
  case Kbps32 extends V1L3Bitrate(1)
  case Kbps40 extends V1L3Bitrate(2)
  case Kbps48 extends V1L3Bitrate(3)
  case Kbps56 extends V1L3Bitrate(4)
  case Kbps64 extends V1L3Bitrate(5)
  case Kbps80 extends V1L3Bitrate(6)
  case Kbps96 extends V1L3Bitrate(7)
  case Kbps112 extends V1L3Bitrate(8)
  case Kbps128 extends V1L3Bitrate(9)
  case Kbps160 extends V1L3Bitrate(10)
  case Kbps192 extends V1L3Bitrate(11)
  case Kbps224 extends V1L3Bitrate(12)
  case Kbps256 extends V1L3Bitrate(13)
  case Kbps320 extends V1L3Bitrate(14)

object V1L3Bitrate {
  def apply(i: Int) =
    V1L3Bitrate.values.find(_.num == i).toRight("Bitrate Invalid")

  implicit val encoder: JsonEncoder[V1L3Bitrate] = JsonEncoder[String].contramap(_.toString)
}

enum SamplingRate(val num: Int, val version: Version):
  case Hz44100 extends SamplingRate(0, V1)
  case Hz48000 extends SamplingRate(1, V1)
  case Hz32000 extends SamplingRate(2, V1)
  case Hz22050 extends SamplingRate(0, V2)
  case Hz24000 extends SamplingRate(1, V2)
  case Hz16000 extends SamplingRate(2, V2)
  case Hz11025 extends SamplingRate(0, V2_5)
  case Hz12000 extends SamplingRate(1, V2_5)
  case Hz8000 extends SamplingRate(2, V2_5)

object SamplingRate {
  def apply(i: Int, version: Version) =
    SamplingRate.values.find(sr => sr.num == i && sr.version == version).toRight("SamplingRate is Invalid")
  implicit val encoder: JsonEncoder[SamplingRate] = JsonEncoder[String].contramap(_.toString)
}

enum ChannelMode(val num: Int):
  case Stereo extends ChannelMode(0)
  case JointStereo extends ChannelMode(1)
  case DualChannel extends ChannelMode(2)
  case SingleChannel extends ChannelMode(3)

object ChannelMode {
  def apply(i: Int) =
    ChannelMode.values.find(_.num == i).toRight("Channel Mode is Invalid")
  implicit val encoder: JsonEncoder[ChannelMode] = JsonEncoder[String].contramap(_.toString)
}

case class FrameHeader(version: Version, layer: Layer, bitrate: V1L3Bitrate, samplingRate: SamplingRate, channelMode: ChannelMode)


object FrameHeader {
  implicit val encoder: JsonEncoder[FrameHeader] = DeriveJsonEncoder.gen[FrameHeader]
}


class FrameHeaderCodec extends Codec[FrameHeader] {
  override def encode(value: FrameHeader): Attempt[BitVector] = ???

  override def sizeBound: SizeBound = SizeBound(80, None)

  override def decode(bits: BitVector): Attempt[DecodeResult[FrameHeader]] = {
    val headerCodec: Codec[(Unit, Int, Int, Boolean, Int, Int, Boolean, Boolean, Int)] =
      ("Sync" | constant(bin"1111 1111 111")) ::
        ("Version" | uint2) ::
        ("Layer" | uint2) ::
        bool ::
        ("Bitrate" | uint4) ::
        ("SamplingRate" | uint2) ::
        bool ::
        bool ::
        uint2

    println(headerCodec.decode(bits))

    headerCodec.decode(bits) match
      case Attempt.Successful(value) => {
        val v = value.value
        val validate = for {
          version <- Version(v._2)
          layer <- Layer(v._3)
          bitRate <- V1L3Bitrate(v._5)
          samplingRate <- SamplingRate(v._6, version)
          channelMode <- ChannelMode(v._9)
        } yield Attempt.Successful(
          DecodeResult(
            FrameHeader(version, layer, bitRate, samplingRate, channelMode), value.remainder
          )
        )
        validate match
          case Left(value) => Attempt.Failure(Err(value))
          case Right(value) => value
      }
      case Attempt.Failure(cause) => Attempt.Failure(cause)
  }
}