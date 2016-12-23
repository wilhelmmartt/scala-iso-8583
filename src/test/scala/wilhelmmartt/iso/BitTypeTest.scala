package wilhelmmartt.iso

import org.scalatest.{Matchers, WordSpec}

import scala.util.{Failure, Success}
import BitType._

/**
  * Created by wll on 17/10/16.
  */
class BitTypeTest extends WordSpec with Matchers {

  val (bitMapParser, bitMapReducer) = bitMap

  "A BitMap " must {

    " return error for invalid buffer " in {
      bitMapParser("") shouldBe Failure(ParserException.invalidBitmap)
    }

    " return error for buffer lesser then 16 " in {
      bitMapParser("B220") shouldBe Failure(ParserException.invalidBitmap)
    }

    " parser a single bit map " in {
      val bitMap = "3220000000100000"
      bitMapParser(bitMap) shouldBe Success((List(3, 4, 7, 11, 44), ""))
    }

    " parser a extended bit map " in {
      val bitMap = "B2200000001000000000000000800000"
      bitMapParser(bitMap) shouldBe Success((List(3, 4, 7, 11, 44, 105), ""))
    }

    " generate a bitmap " in {
      val lst = List(3, 4, 7, 11, 44)
      bitMapReducer(lst) shouldBe Success("3220000000100000")
    }

    " generate a extended bitmap " in {
      val lst = List(3, 4, 7, 11, 44, 105)
      bitMapReducer(lst) shouldBe Success("b2200000001000000000000000800000")
    }

  }

  " A fixed bit" must {

    " read a fixed number of elements " in {
      val (bitParser, _) = fixed(7)
      val Success((r, _)) = bitParser("0123456789")
      r shouldBe "0123456"
    }

    " generate fixed number of elements " in {
      val (_, bitReducer) = fixed(5)
      bitReducer("01234") shouldBe Success("01234")
    }

    " fail to no elements " in {
      val (_, bitReducer) = fixed(3)
      bitReducer("") shouldBe Failure(ReducerException.noData)
    }

    " fail to elements lesser than fixed " in {
      val (_, bitReducer) = fixed(3)
      bitReducer("01") shouldBe Failure(ReducerException.lengthDiffers(2,3))
    }

  }

  "A var bit" must {

    " read a data with length prefix " in {
      val (bitParser, _) = variable(4)
      val Success((r, _)) = bitParser("000205")

      r shouldBe "05"
    }

    " generate data and length prefix " in {
      val (_, bitReducer) = variable(3)
      bitReducer("100") shouldBe Success("003100")
    }

    " generate zero prefix to no elements " in {
      val (_, bitReducer) = variable(2)
      bitReducer("") shouldBe Success("00")
    }

    " fail to elements greater than supported " in {
      val (_, bitReducer) = variable(2)
      val data = Array.fill(150)("0").mkString
      bitReducer(data) shouldBe Failure(ReducerException.tooLong(99, 150))
    }

  }

}
