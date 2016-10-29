package wilhelmmartt.iso

import wilhelmmartt.iso.BitDef._
import org.scalatest.{Matchers, WordSpec}

import scala.util.{Failure, Success}

/**
  * Created by wilhelmmartt on 05/10/16.
  */
class ISODefTest extends WordSpec with Matchers {

  " A ISODef " must {

    " parse a message " in {
      val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        44 -> ll,
        105 -> lll
      )

      val msg = "0200B2200000001000000000000000800000201234000000010000110722183012345606A5DFGR021ABCDEFGHIJ 1234567890"
      val Success(isoMsg) = ISODef.parse(isoDef)(msg)

      isoMsg(105) shouldBe "ABCDEFGHIJ 1234567890"
    }

    " fail to parse a message " in {
      val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        44 -> ll,
        105 -> fixed(4)
      )

      val msg = "0200B2200000001000000000000000800000201234000000010000110722183012345606A5DFGR021"
      ISODef.parse(isoDef)(msg) shouldBe Failure(ParserException("Error bit 105: Invalid Buffer"))
    }

    " reduce a message " in {
      val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        39 -> fixed(2),
        63 -> lll
      )

      val isoMsg = Map(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        63 -> "ABC"
      )

      val res = ISODef.reduce(isoDef)(isoMsg)
      res shouldBe Success("020032200000000000020000010000000001001012194700123456003ABC")
    }

    " fail to reduce a message " in {
      val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        39 -> fixed(2),
        63 -> lll
      )

      val isoMsg = Map(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        39 -> "ABC"
      )

      val res = ISODef.reduce(isoDef)(isoMsg)
      res shouldBe Failure(ReducerException("Error bit 39: Data length (3) differs from field length (2)"))
    }

    " reduce and parse a message " in {

      val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        39 -> fixed(2),
        63 -> lll,
        102 -> lll
      )

      val isoMsg = Map(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        63 -> "ABC",
        102 -> "YEAHH"
      )

      val Success(reduced) = ISODef.reduce(isoDef)(isoMsg)
      val Success(parsed) = ISODef.parse(isoDef)(reduced)

      parsed(102) shouldBe isoMsg(102)

    }
  }

}
