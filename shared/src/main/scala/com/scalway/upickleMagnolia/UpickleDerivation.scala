package com.scalway.upickleMagnolia

package rabbi

import magnolia._
import ujson.{ObjVisitor, Visitor}
import upickle.{Api, AttributeTagged}

import scala.language.experimental.macros
import scala.reflect.ClassTag

trait UpickleSupportCommon {
  type Typeclass[T]
  type CC[T] = CaseClass[Typeclass, T]
  type ST[T] = SealedTrait[Typeclass, T]

  @deprecatedOverriding("dont use it from your code. It is intendet to be used only by " +
    "magnolia and is visible 4 u just because i couldn't figure out how to hide it successfully.", "0.1")
  protected val upickleApi0:Api

  private def key_@(arg:Seq[Any]) = arg.collectFirst {
    case x:upickle.key => x.s
  }

  /**
    * TODO make all methods protected! We cannot do that due to UpickleDerivationMixin but
    * it is deprecated and probably would be deleted in near future!
    */
  def ccName[T](ctx:CC[T]) = {
    key_@(ctx.annotations).getOrElse(ctx.typeName.full)
  }

  def paramLabel[F[_], E](arg:Param[F, E]) = {
    key_@(arg.annotations).getOrElse(arg.label)
  }

  def dispatch0[T](ctx: ST[T]) = ctx.subtypes.map(_.typeclass)
}

trait UpickleSupportR extends UpickleSupportCommon {
  import upickleApi0._
  type Typeclass[T] <: Reader[T]

  class TaggedReader2[T](ctx:CC[T]) extends TaggedReader.Leaf[T](ccName[T](ctx), deriveCaseR(ctx)) {
    val name = ccName(ctx)
  }

  def deriveCaseRTagged[T](ctx:CC[T]): TaggedReader2[T] = new TaggedReader2[T](ctx)

  def deriveCaseR[T](ctx:CC[T]) = JsObjR.map[T] { r =>
    ctx.construct(p => {
      val paramLabelValue = paramLabel(p)
      r.value.get(paramLabelValue) match {
        case Some(x) => readJs(x)(p.typeclass)
        case None => p.default.getOrElse(throw new Exception("missing argument: " + paramLabelValue))
      }
    })
  }
}

trait UpickleSupportW extends UpickleSupportCommon {
  import upickleApi0._
  type Typeclass[T] <: Writer[T]

  def deriveCaseWTagged[T:ClassTag](ctx:CC[T]): TaggedWriter[T] = {
    new TaggedWriter.Leaf[T](implicitly, ccName[T](ctx), deriveCaseW(ctx))
  }

  def deriveCaseW[T](ctx:CC[T]) = new CaseW[T] {
    override def writeToObject[R](ww: ObjVisitor[_, R], v: T): Unit = {
      ctx.parameters.zipWithIndex.foreach { case (arg, i) =>
        val argWriter = arg.typeclass
        val value = arg.dereference(v)
        if (!arg.default.contains(value)) {
          ww.visitKey(objectAttributeKeyWriteMap(paramLabel(arg)), -1)
          ww.visitValue(
            argWriter.write(
              ww.subVisitor.asInstanceOf[Visitor[Any, Nothing]],
              value
            ),
            -1
          )
        }
      }
    }
  }
}

class UpickleDerivationW[A <: Api](override val upickleApi0:Api) extends UpickleSupportW {
  import upickleApi0._
  type Typeclass[T] = Writer[T]
  def combine[T:ClassTag](ctx: CC[T]): Typeclass[T] = deriveCaseWTagged(ctx)
  def dispatch[T](ctx: ST[T]): Typeclass[T] = Writer.merge[T](dispatch0(ctx) :_*)

  implicit def autoWriter[T]: Typeclass[T] = macro Magnolia.gen[T]
  def writerOf[T]: Typeclass[T] = macro Magnolia.gen[T]
}

class UpickleDerivationR[A <: Api](override val upickleApi0:A) extends UpickleSupportR {
  import upickleApi0._
  type Typeclass[T] = Reader[T]
  def combine[T:ClassTag](ctx: CC[T]): Typeclass[T] = deriveCaseRTagged(ctx)
  def dispatch[T](ctx: ST[T]): Typeclass[T] = Reader.merge[T](dispatch0(ctx) :_*)

  implicit def autoReader[T]: Typeclass[T] = macro Magnolia.gen[T]
  def readerOf[T]: Typeclass[T] = macro Magnolia.gen[T]
}

class UpickleDerivation[A <: Api](override val upickleApi0:A) extends UpickleSupportW with UpickleSupportR {
  import upickleApi0._
  type Typeclass[T] = ReadWriter[T]
  def combine[T:ClassTag](ctx: CC[T]): Typeclass[T] = {
    new TaggedReader2[T](ctx) with TaggedReadWriter[T] {
      val reader = deriveCaseR(ctx)
      val writer = deriveCaseW(ctx)
      override def findReader(s: String) = if (name == s) reader else null
      override def findWriter(v: Any): (String, upickleApi0.CaseW[T]) =
        if (implicitly[ClassTag[T]].runtimeClass.isInstance(v)) (name -> writer)
        else null
    }
  }

  def dispatch[T](ctx: ST[T]): Typeclass[T] =  {
    val allDispatches = dispatch0(ctx).asInstanceOf[Seq[TaggedReadWriter[T]]]
    new TaggedReadWriter.Node[T](allDispatches :_*) {
      override def findReader(s: String): upickleApi0.Reader[T] = allDispatches.collectFirst {
        case x if x.findReader(s) != null => x.findReader(s)
      }.getOrElse(null)
      override def findWriter(v: Any): (String, upickleApi0.CaseW[T]) = super.findWriter(v)
    }
  }

  implicit def autoReadWriter[T]: Typeclass[T] = macro Magnolia.gen[T]
  def readWriterOf[T]: Typeclass[T] = macro Magnolia.gen[T]
}

@deprecated(
  """use with caution! It is only reason why we have public fields in Support classes and they leaks with import.""".stripMargin, "0.1")
trait UpickleDerivationMixin { self:Api =>
  type Typeclass[T] = ReadWriter[T]

  protected val deriveSupport = new UpickleSupportW with UpickleSupportR {
    val upickleApi0:self.type = self
    type Typeclass[T] = upickleApi0.Typeclass[T]
  }

  import deriveSupport._

  def combine[T:ClassTag](ctx: CC[T]): Typeclass[T] = ReadWriter.join[T](deriveCaseRTagged(ctx), deriveCaseWTagged(ctx))
  def dispatch[T](ctx: ST[T]): Typeclass[T] = ReadWriter.merge[T](dispatch0(ctx) :_*)
  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
