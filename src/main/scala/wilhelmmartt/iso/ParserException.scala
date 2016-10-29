package wilhelmmartt.iso

/** Exceptions occurred while parsing bits.
  *
  * Created by wilhelmmartt on 09/10/16.
  */
private[iso] case class ParserException(reason: String) extends IllegalStateException(reason)

object ParserException {
  def invalidBitmap = ParserException("Invalid BitMap")
  def invalidBuffer = ParserException("Invalid Buffer")
  def rethrow(newReason: String, re: Throwable) = new ParserException(newReason + re.getMessage)
}