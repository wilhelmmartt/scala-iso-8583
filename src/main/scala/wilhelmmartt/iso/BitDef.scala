package wilhelmmartt.iso

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import Utils._

/** This object contains all bit definitions (parsers and reductions) needed to create a iso definition.
  *
  * Created by wilhelmmartt on 09/10/16.
  */
object BitDef {

  /** A special type of bit definition for ISOs. It is used to inform which bits are currently in a message.
    * Parser: It receives a buffer, parse, checking if it has an extended bitmap. It returns the List of bits and rest of buffer.
    * Reducer: Receives a List of bits which represents all bits in current message. It returns a bit map.
    *
    * @return (BitMapParser, BitMapReducer)
    */
  def bitMap : BitMapOps = {
    def normalize(lst: List[Int], bitMapN: Int) = lst.map(_ + (bitMapN * maxBitPerBitMap))
    def extended(lst: List[Int]) = lst.head == 1

    @tailrec
    def parse(s: String, pbm: List[Int], bitMapN: Int) : Try[(List[Int], String)] = {
      Try {
        s.splitAt(bitMapSize)
      } match {
        case Success((bms, ts)) if bms.isEmpty || bms.length < bitMapSize => Failure(ParserException.invalidBitmap)
        case Success((bms, ts)) =>
          fromHexString(bms) match {
            case bitMap if extended(bitMap) => parse(ts, normalize(bitMap.tail, bitMapN), bitMapN + 1)
            case bitMap =>
              Success((pbm ++ normalize(bitMap, bitMapN), ts))

          }
      }
    }

    def reduce : BitMapReducer = (bitMap: List[Int]) => {
      val ba = Array.fill(bitMapSize)(0.toByte)

      val bitArray = if (bitMap.last <= 63)  {
        ba.drop(bitMapSize / 2)
      } else {
        ba(0) = refBits.head
        ba
      }

      Success(toHexString(bitMap, bitArray))
    }

    (parse(_, List(), 0), reduce)
  }

  /** Definition for fixed data length.
    *
    * @param len length of data supported.
    * @return (BitParser, BitReducer)
    */
  def fixed(len: Int): BitOps = {
    ((buffer: String) => {
      if (buffer.length < len) Failure(ParserException.invalidBuffer)
      else Success(buffer.splitAt(len))

    }, (data: String) => {
      if (data.isEmpty) Failure(ReducerException.noData)
      else if (data.length != len) Failure(ReducerException.lengthDiffers(data.length, len))
      else Success(data)
    })
  }

  /** Definition which data has prepended its length
    *
    * @param len length of prefix
    * @return (BitParser, BitReducer)
    */
  def variable(len: Int) : BitOps = {
    val lenPrefix = s"%0${len}d"
    val maxLen = Integer.parseInt(Array.fill(len)("9").mkString)

    ((buffer: String) => {
      if (buffer.length < len) Failure(ParserException.invalidBuffer)
      else {
        val (l, rb) = buffer.splitAt(len)
        val bl = l.toInt

        if (rb.length < bl) Failure(ParserException.invalidBuffer)
        else Success(rb.splitAt(l.toInt))
      }
    }, (data: String) => {
      if (data.length > maxLen) Failure(ReducerException.tooLong( maxLen, data.length))
      else Success(lenPrefix.format(data.length) + data)
    })
  }

  /** Special case of variable length 2
    *
    * @return (BitParser, BitReducer)
    */
  def ll : BitOps = variable(2)

  /** Special case of variable length 3
    *
    * @return (BitParser, BitReducer)
    */
  def lll : BitOps = variable(3)

}
