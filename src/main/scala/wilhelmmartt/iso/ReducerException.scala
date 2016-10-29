package wilhelmmartt.iso

/** Exceptions occurred while reducing bits.
  *
  * Created by wilhelmmartt on 09/10/16.
  */
private[iso] case class ReducerException(reason: String) extends IllegalStateException(reason)

object ReducerException {
  def noData = new ReducerException("Data is Empty")
  def lengthDiffers(dataLen: Int, fieldLen: Int) = new ReducerException(s"Data length ($dataLen) differs from field length ($fieldLen)")
  def tooLong(maxLen: Int, currLen: Int) = new ReducerException(s"Invalid size. Max: $maxLen, current: $currLen")
  def rethrow(newReason: String, re: Throwable) = new ReducerException(newReason + re.getMessage)
}
