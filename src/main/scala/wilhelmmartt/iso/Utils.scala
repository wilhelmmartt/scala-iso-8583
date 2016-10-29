package wilhelmmartt.iso

/** Utils object
  *
  * Created by wilhelmmartt on 15/10/16.
  */
object Utils {

  def fromHexString(hexString: String) : List[Int] = {
    def fromHex = hexString.sliding(2,2).toArray.map(Integer.parseInt(_, 16).toByte)
    def singleBitMap(c: Byte) = refBits.map(b => {
      if ((c & b) == b) 1 else 0
    })

    val l = fromHex
      .flatMap(singleBitMap)
      .zipWithIndex
      .filter(_._1 > 0)
      .map(_._2 + 1)
      .toList

    l
  }

  def toHexString(bits: List[Int], bitArray: Array[Byte]) : String =  {
    bits.map(_ - 1).foreach { b =>
      val pos = b / 8
      val refBit = refBits(b % 8)
      bitArray(pos) = (bitArray(pos) | refBit).toByte
    }

    bitArray.map(b => String.format("%02x", Byte.box(b))).mkString
  }

}
