package upickle
import com.scalway.upickleMagnolia.rabbi.{UpickleDerivation, UpickleDerivationR, UpickleDerivationW}
import utest._

/**
* Created by haoyi on 4/22/14.
*/
object TestUtilAuto extends TestUtil[upickle.default.type](upickle.default) {
  val derive = new UpickleDerivation[upickle.default.type](upickle.default)
  val deriveR = new UpickleDerivationR[upickle.default.type](upickle.default)
  val deriveW = new UpickleDerivationW[upickle.default.type](upickle.default)
}

object TestUtil extends TestUtil[upickle.default.type](upickle.default)
object LegacyTestUtil extends TestUtil[upickle.legacy.type](upickle.legacy)
class TestUtil[Api <: upickle.Api](val api: Api){
  import api._
  def rw1[T: Reader: Writer](t: T, s: String) = {
    val parsed = read[T](s)
    assert(parsed == parsed)
  }

  def rw[T: Reader: Writer](t: T, s: String*) = {
    rwk[T, T](t, s:_*)(x => x)
  }
  def rwk[T: Reader: Writer, V](t: T, sIn: String*)(normalize: T => V) = {
    val writtenT = write(t)

    val strings = sIn.map(_.trim)

//    if (strings.length > 0) {
//      val inputs = strings.map(api.read)
//      val output = api.read(writtenT)
//      assert(inputs.contains(output))
//    }
    for (s <- strings) {
      val readS = read[T](s)
      val normalizedReadString = normalize(readS)
      val normalizedValue = normalize(t)
      assert(normalizedReadString == normalizedValue)
    }

    val normalizedReadWrittenT = normalize(read[T](writtenT))
    val normalizedT = normalize(t)
    assert(normalizedReadWrittenT == normalizedT)
  }
}