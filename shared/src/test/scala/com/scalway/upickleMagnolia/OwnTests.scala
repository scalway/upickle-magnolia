package com.scalway.upickleMagnolia

import ujson.Transformable
import utest._
import upickle.default._
import upickle.TestUtilAuto.derive.autoReadWriter
import upickle.key



object OwnTests extends TestSuite {
  sealed trait Msg
  @key("One") case class OneMsg(text:String) extends Msg
  @key("Multi") case class MultipleMsg(text:Seq[Msg]) extends Msg
  @key("Term") case object TerminationMessage extends Msg


  def rw1[T:Reader:Writer](pureValue:T, exStr:String) = {
    val pureStr = write(pureValue)
    val exValue = read[T](exStr)
    assert(pureStr == exStr)
    assert(pureValue == exValue)
  }

  case class MsgT[T](text:T)

  override def tests: Tests = Tests {
    implicit lazy val msgRW = autoReadWriter[Msg]
    //read[Msg]("""{"$type":"com.scalway.upickleMagnolia.OwnTests.OneMsg","text":"yes"}""")
    "struct" - rw1[Msg](MultipleMsg(Seq(OneMsg("yes"),OneMsg("yes"), OneMsg("yes"))),
      """{"$type":"Multi","text":[{"$type":"One","text":"yes"},{"$type":"One","text":"yes"},{"$type":"One","text":"yes"}]}""")
    "msgT" - rw1(MsgT("yes"), "{\"$type\":\"com.scalway.upickleMagnolia.OwnTests.MsgT\",\"text\":\"yes\"}")
  }

}
