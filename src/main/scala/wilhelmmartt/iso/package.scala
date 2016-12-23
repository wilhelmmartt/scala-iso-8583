package wilhelmmartt

import scala.util.Try

/** This package object contains all types used. It also contains some constants too.
  *
  * Created by wilhelmmartt on 15/10/16.
  */
package object iso {
  type BitParser = String => Try[(String, String)]
  type BitReducer = String => Try[String]
  type BitOps = (BitParser, BitReducer)
  type BitDef = (Int, BitOps)

  type BitMapParser = String => Try[(List[Int], String)]
  type BitMapReducer = List[Int] => Try[String]
  type BitMapOps = (BitMapParser, BitMapReducer)

  type ISOMsgList = List[(Int, String)]

  val bitMapSize = 16
  val maxBitPerBitMap = 64
  val mtiBit = 0
  val bitMapBit = 1

  private[iso] val refBits = Array(128,64,32,16,8,4,2,1).map(_.toByte)

}
