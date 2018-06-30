
# upickle-magnolia

This is experimental library that brings power of magnolia to upickle serialisation library.

Currently it is in development and ... to be honest doesnt work well. 
Dont use it right now but keep eye on it :).


How to use it (when all error'll be fixed)
---------------------
```scala

//Preffered way of using it is create special object in your Api class that'll bring
//deriviation to your application when imported.
object UpApi extends AttributeTagged {
  val derive = new UpickleDerivation(this)
}
//and usage example
import UpApi._
import UpApi.derive.autoReadWriter
implicit val msgRW = implicitly[ReadWriter[Message]]

//if `Message` is recursive in more complicated way use lazy val instead!
implicit lazy val msgRW = implicitly[ReadWriter[Message]]

// If you are not interested in automatic deriviation then import 'readWriterOf' method
import UpApi._
import UpApi.derive.readWriterOf
implicit val msgRW = readWriterOf[Message]

//If you need only Reader or Writer You can create just instance that creates it.
object UpApi2 extends AttributeTagged {
  val deriveW = new UpickleDerivationW(this)
  val deriveR = new UpickleDerivationR(this)
}

import UpApi2._
import UpApi2.deriveW.autoWriter
implicit val msgW = implicitly[Writer[Message]]

//or more explicitly:
import UpApi2._
import UpApi2.deriveW.writerOf
implicit val msgW = writerOf[Message]
```