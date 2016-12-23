package wilhelmmartt.iso

/** Class represents a iso message
  *
  * Created by wilhelmmartt on 19/12/16.
  */
class ISOMsg (private val fields: Map[Int, String]) {
  def apply(bit: Int) : String = fields(bit)
  def + (f: (Int, String)) : ISOMsg = new ISOMsg(fields + f)
  def toList : List[(Int, String)] = fields.toList
}

object ISOMsg {
  def apply(f: (Int, String)*) : ISOMsg = new ISOMsg(f.toMap)
}
