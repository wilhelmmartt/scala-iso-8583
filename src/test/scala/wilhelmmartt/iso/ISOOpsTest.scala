package wilhelmmartt.iso

import wilhelmmartt.iso.BitType._
import org.scalatest.{Matchers, WordSpec}

import scala.util.{Failure, Success}

/**
  * Created by wilhelmmartt on 05/10/16.
  */
class ISOOpsTest extends WordSpec with Matchers {

  def parserFixture = {
      0 ~> 4 ::
      bitMap ::
      3 ~> 6 ::
      4 ~> 12 ::
      7 ~> 10 ::
      11 ~> 6 ::
      44 ~> ll ::
      105 ~> lll ::
      EndD
  }

  def reducerFixture = {
      0 ~> 4 ::
      bitMap ::
      3 ~> 6 ::
      4 ~> 12 ::
      7 ~> 10 ::
      11 ~> 6 ::
      39 ~> 2 ::
      63 ~> lll ::
      102 ~> lll ::
      EndD
  }

  " A ISODef " must {

    " parse a message " in {
      val msg = "0200B2200000001000000000000000800000201234000000010000110722183012345606A5DFGR021ABCDEFGHIJ 1234567890"
      val Success(isoMsg) = parserFixture.parse(msg)

      isoMsg(105) shouldBe "ABCDEFGHIJ 1234567890"
    }

    " fail to parse a message " in {
      val msg = "0200B2200000001000000000000000800000201234000000010000110722183012345606A5DFGR021"
      parserFixture.parse(msg) shouldBe Failure(ParserException("Error bit 105: Invalid Buffer"))
    }

    " reduce a message " in {
      val isoMsg = ISOMsg(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        63 -> "ABC"
      )

      val res = reducerFixture.reduce(isoMsg)
      res shouldBe Success("020032200000000000020000010000000001001012194700123456003ABC")
    }

    " fail to reduce a message " in {
      val isoMsg = ISOMsg(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        39 -> "ABC"
      )

      val res = reducerFixture.reduce(isoMsg)
      res shouldBe Failure(ReducerException("Error bit 39: Data length (3) differs from field length (2)"))
    }

    " reduce and parse a message " in {
      val isoMsg = ISOMsg(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        63 -> "ABC",
        102 -> "YEAHH"
      )

      val Success(reduced) = reducerFixture.reduce(isoMsg)
      val Success(parsed) = reducerFixture.parse(reduced)

      parsed(102) shouldBe isoMsg(102)

    }

    " reduce an invalid message " in {
      val isoMsg = ISOMsg(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        11 -> "123456",
        35 -> "ABC"
      )

      val res = reducerFixture.reduce(isoMsg)
      res shouldBe Failure(ReducerException.invalidMessage)

    }
  }

}
