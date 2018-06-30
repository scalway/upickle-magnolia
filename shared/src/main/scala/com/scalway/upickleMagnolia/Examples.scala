package com.scalway.upickleMagnolia

import com.scalway.upickleMagnolia.rabbi.{UpickleDerivation, UpickleDerivationMixin, UpickleDerivationR, UpickleDerivationW}
import upickle.AttributeTagged

object Examples {
  //-----------------------------------------------------------------
  //                 HOW TO USE IT
  //-----------------------------------------------------------------

  /**
    * Preffered way of using it is create special object in your Api class that'll bring
    * deriviation to your application when imported.
    *
    * {{{
    *   import UpApi._
    *   import UpApi.derive.autoReadWriter
    *
    *   implicit val msgRW = implicitly[ReadWriter[Message]]
    * }}}
    *
    * if `Message` is recursive in more complicated way use lazy val instead!
    *
    * {{{
    *   implicit lazy val msgRW = implicitly[ReadWriter[Message]]
    * }}}
    *
    * If you are not interested in automatic deriviation then import upickleDerive method
    *
    * {{{
    *   import UpApi._
    *   import UpApi.derive.readWriterOf
    *   implicit val msgRW = readWriterOf[Message]
    * }}}
    * */
  object UpApi extends AttributeTagged {
    val derive = new UpickleDerivation(this)
  }

  /**
    * If you need only Reader or Writer You can create just instance that creates it.
    *
    * {{{
    *   import UpApi._
    *   import UpApi.deriveW.autoReadWriter
    *   implicit val msgW = implicitly[Writer[Message]]
    * }}}
    *
    * or more explicitly:
    *
    * {{{
    *   import UpApi._
    *   import UpApi.deriveW.writerOf
    *   implicit val msgW = writerOf[Message]
    * }}}
    * */

  //If you need only Reader or Writer You can create just instance that creates it.
  object UpApi2 extends AttributeTagged {
    val deriveW = new UpickleDerivationW(this)
    val deriveR = new UpickleDerivationR(this)
  }

  /**
    * You can also simply create derivation for existing api and use it with companion to it
    *
    * {{{
    *   import upickle.default._
    *   import UpDerive.autoReadWriterOf
    *
    *   implicit val msgRW = implicitly[ReadWriter[Message]]
    * }}}
    */
  object UpDerive extends UpickleDerivation(upickle.default)

  /**
    * You can use UpickleDerivationMixin to create own api with automatic
    * deriviation turned on by default. It is risky and should be avoided i guess
    * because it can create so much garbage for you... but you can.
    *
    * {{{
    *   import UpApiAuto._
    *
    *   implicit val msgRW = implicitly[ReadWriter[Message]]
    * }}}
    * */
  object UpApiAuto extends AttributeTagged with UpickleDerivationMixin {}
}


