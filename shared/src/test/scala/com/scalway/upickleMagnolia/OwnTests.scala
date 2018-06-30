package com.scalway.upickleMagnolia

import com.scalway.upickleMagnolia.OwnTests.Msg
import utest._
import upickle.default._
import upickle.TestUtilAuto.derive.autoReadWriter
import upickle.key

case class OneMsg(text:String) extends Msg
@key("Multi") case class MultipleMsg(text:Seq[Msg]) extends Msg

object OwnTests extends TestSuite {
  sealed trait Msg

  def rw1[T:Reader:Writer](pureValue:T, exStr:String) = {
    val pureStr = write(pureValue)
    //val exValue = read[T](exStr)
    assert(pureStr == exStr)
    //assert(pureValue == exValue)
  }

  case class MsgT[T](text:T)

  override def tests: Tests = Tests {
    implicit val msgRW = autoReadWriter[Msg]
    "struct" - rw1(MultipleMsg(Seq(OneMsg("yes"),OneMsg("yes"), OneMsg("yes"))), """{"$type":"com.scalway.upickleMagnolia.OneMsg","text":"yes"}""")
    //"msgT" - rw1(MsgT("yes"), "{\"$type\":\"com.scalway.upickleMagnolia.OwnTests.MsgT\",\"text\":\"yes\"}")
  }

}
