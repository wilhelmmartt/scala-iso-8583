package wilhelmmartt.iso

import scala.util.Try

/** Grammar to construct iso defenitions
  *
  * Created by wilhelmmartt on 20/12/16.
  */
trait Def {
  def :: (s: BitDef) : BitD = BitD(s, this)
  def :: (s: BitMapOps) : BitM = BitM(s, this)

  def parse : String => Try[ISOMsg] = ISOOps.parse(this)
  def reduce : ISOMsg => Try[String] = ISOOps.reduce(this)
}

/** Normal bit definition
  *
  * @param ops
  * @param tail
  */
case class BitD(ops: BitDef, tail: Def) extends Def

/** Bit map definition
  *
  * @param ops
  * @param tail
  */
case class BitM(ops: BitMapOps, tail: Def) extends Def

case object EndD extends Def
