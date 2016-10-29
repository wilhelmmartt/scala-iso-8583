package wilhelmmartt.iso

import wilhelmmartt.iso.BitDef._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/** Class ISODef holds a Iso definition.
  *
  * Created by wilhelmmartt on 16/10/16.
  */
class ISODef(val mti: BitOps, val bitMap: BitMapOps, bits: Map[Int, BitOps]) {
  def apply(bit: Int) = bits(bit)

  def get(bit: Int) = bits.get(bit)
}

/** Object ISODef is responsible to create, parse and reduce ISO Definitions.
  *
  */
object ISODef {

  /** Create a new standard definition with a mti (length 4), a bit map and others definitions informed.
    *
    * @param defs a seq of bit definitions
    * @return a new ISODef
    */
  def apply(defs: (Int, BitOps)*) = new ISODef(fixed(4), bitMap, defs.toMap)

  /** Parse an ascii string to ISOMsg
    *
    * @param isoDef a Iso Definition
    * @param buffer a serialized ISOMsg
    * @return a ISOMsg
    */
  def parse(isoDef: ISODef)(buffer: String): Try[ISOMsg] = {
    val (mtiParser: BitParser, _) = isoDef.mti
    val (bitMapParser: BitMapParser, _) = isoDef.bitMap

    for {
      (mti, rb) <- mtiParser(buffer)
      (bitMap, rb) <- bitMapParser(rb)
      msg <- parseMsg(Map(mtiBit -> mti), bitMap, isoDef, rb)
    } yield msg
  }

  @tailrec
  private final def parseMsg(isoMsg: ISOMsg, bits: List[Int], isoDef: ISODef, buffer: String): Try[ISOMsg] = bits match {
    case Nil => Success(isoMsg)
    case bit :: tail =>
      val (isoParser: BitParser, _) = isoDef(bit)
      isoParser(buffer) match {
        case Success((bitValue: String, rb)) => parseMsg(isoMsg + (bit -> bitValue), tail, isoDef, rb)
        case Failure(t) => Failure(ParserException.rethrow(s"Error bit $bit: ", t))
      }
  }

  /** Reduce a ISOMsg to an ascii string
    *
    * @param isoDef a Iso Definition
    * @param isoMsg a ISOMsg
    * @return a serialized ISOMsg
    */
  def reduce(isoDef: ISODef)(isoMsg: ISOMsg): Try[String] = {
    val (_, mtiReducer: BitReducer) = isoDef.mti
    val (_, bitMapReducer: BitMapReducer) = isoDef.bitMap
    val _ :: isoMsgList = isoMsg.toList.sortBy(_._1)

    for {
      mti <- mtiReducer(isoMsg(mtiBit))
      bitMap <- bitMapReducer(isoMsgList.map(_._1))
      vals <- reduceMsg(isoDef)(isoMsgList)(Success(List()))
    } yield mti + bitMap + vals.mkString
  }

  @tailrec
  final def reduceMsg(isoDef: ISODef)(isoMsg: ISOMsgList)(result: Try[List[String]]) : Try[List[String]] = isoMsg.size match {
    case 0 => result
    case _ =>
      val (bit, value) = isoMsg.head

      isoDef(bit)._2(value) match {
        case Success(v) => reduceMsg(isoDef)(isoMsg.tail)(result.map(_ :+ v))
        case Failure(t) => Failure(ReducerException.rethrow(s"Error bit $bit: ", t))
      }
  }

}
