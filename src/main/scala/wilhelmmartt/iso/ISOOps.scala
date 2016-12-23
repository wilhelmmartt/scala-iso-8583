package wilhelmmartt.iso

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/** Object ISODef is responsible to create, parse and reduce ISO Definitions.
  *
  * Created by wilhelmmartt on 16/10/16.
  */
private[iso] object ISOOps {

  /** Parse an ascii string to ISOMsg
    *
    * @param isoDef a Iso Definition
    * @param buffer a serialized ISOMsg
    * @return a ISOMsg
    */
  def parse(isoDef: Def)(buffer: String): Try[ISOMsg] = {
    val BitD(mtiDef, BitM(bitMapDef, tailDefs)) = isoDef

    val (mtiParser: BitParser, _) = mtiDef._2
    val (bitMapParser: BitMapParser, _) = bitMapDef

    for {
      (mti, rb) <- mtiParser(buffer)
      (bitMap, rb) <- bitMapParser(rb)
      msg <- parseMsg(ISOMsg(mtiBit -> mti), bitMap, tailDefs, rb)
    } yield msg
  }

  @tailrec
  private final def parseMsg(isoMsg: ISOMsg, bits: List[Int], isoDef: Def, buffer: String): Try[ISOMsg] = bits match {
    case Nil => Success(isoMsg)
    case bit :: tail =>
      val BitD(bitDef, tailDefs) = isoDef

      if (bit != bitDef._1) {
        tailDefs match {
          case EndD => Failure(ParserException.invalidBuffer)
          case _ => parseMsg(isoMsg, bits, tailDefs, buffer)
        }
      } else {
        val (isoParser: BitParser, _) = bitDef._2
        isoParser(buffer) match {
          case Success((bitValue: String, rb)) => parseMsg(isoMsg + (bit -> bitValue), tail, tailDefs, rb)
          case Failure(t) => Failure(ParserException.rethrow(s"Error bit $bit: ", t))
        }
      }
  }

  /** Reduce a ISOMsg to an ascii string
    *
    * @param isoDef a Iso Definition
    * @param isoMsg a ISOMsg
    * @return a serialized ISOMsg
    */
  def reduce(isoDef: Def)(isoMsg: ISOMsg): Try[String] = {
    val BitD(mtiDef, BitM(bitMapDef, tailDefs)) = isoDef

    val (_, mtiReducer: BitReducer) = mtiDef._2
    val (_, bitMapReducer: BitMapReducer) = bitMapDef
    val _ :: isoMsgList = isoMsg.toList.sortBy(_._1)

    for {
      mti <- mtiReducer(isoMsg(mtiBit))
      bitMap <- bitMapReducer(isoMsgList.map(_._1))
      vals <- reduceMsg(tailDefs)(isoMsgList)(Success(List()))
    } yield mti + bitMap + vals.mkString
  }

  @tailrec
  private final def reduceMsg(isoDef: Def)(isoMsg: ISOMsgList)(result: Try[List[String]]) : Try[List[String]] = isoMsg.size match {
    case 0 => result
    case _ =>
      val (bit, value) = isoMsg.head
      val BitD(bitDef: BitDef, tailDefs) = isoDef

      if (bit != bitDef._1) {
        tailDefs match {
          case EndD => Failure(ReducerException.invalidMessage)
          case _ => reduceMsg(tailDefs)(isoMsg)(result)
        }
      } else {
        val (_, isoReducer: BitReducer) = bitDef._2

        isoReducer(value) match {
          case Success(v) => reduceMsg(tailDefs)(isoMsg.tail)(result.map(_ :+ v))
          case Failure(t) => Failure(ReducerException.rethrow(s"Error bit $bit: ", t))
        }
      }
  }

}
