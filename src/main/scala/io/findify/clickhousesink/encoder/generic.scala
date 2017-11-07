package io.findify.clickhousesink.encoder

import io.findify.clickhousesink.CustomMapper
import io.findify.clickhousesink.field.Field
import shapeless.{:+:, CNil, Coproduct, HList, HNil, LabelledTypeClass, LabelledTypeClassCompanion, Lazy}

object generic {
  implicit val intEncoder = new IntEncoder()
  implicit val stringEncoder = new StringEncoder()
  implicit val longEncoder = new LongEncoder()
  implicit val floatEncoder = new FloatEncoder()
  implicit val doubleEncoder = new DoubleEncoder()
  implicit val boolEncoder = new BooleanEncoder()

  implicit def arrayEncoderPrimitive[T <: AnyVal](implicit encoder: ScalarEncoder[T]) = new PrimitiveArrayEncoder[T]()
  implicit def arrayEncoderString(implicit encoder: ScalarEncoder[String]) = new StringArrayEncoder()
  implicit def nestedEncoder[T <: Product](implicit encoder: Encoder[T]) = new SeqEncoder[T]()
  implicit def optionEncoder[T](implicit encoder: ScalarEncoder[T]) = new OptionEncoder[T]()

  def deriveEncoder[T](implicit encoder: Lazy[Encoder[T]]) = encoder.value

  object auto extends LabelledTypeClassCompanion[Encoder] {
    object typeClass extends LabelledTypeClass[Encoder] {
      override def emptyProduct: Encoder[HNil] = new Encoder[HNil] {
        override def ddl(name: String, mapper: CustomMapper, separator: String): String = ""
        override def encode(value: HNil): Seq[Field] = Nil
      }

      override def product[H, T <: HList](name: String, ch: Encoder[H], ct: Encoder[T]): Encoder[shapeless.::[H, T]] = new Encoder[shapeless.::[H, T]] {
        override def fieldCount: Int = ch.fieldCount + 1
        override def ddl(xname: String, mapper: CustomMapper, separator: String): String = {
          val headDDL = ch.ddl(name, mapper, separator)
          val tailDDL = ct.ddl("empty", mapper, separator)
          if (tailDDL.isEmpty)
            headDDL
          else
            headDDL + separator + tailDDL
        }
        override def encode(value: shapeless.::[H, T]): Seq[Field] = ch.encode(value.head) ++ ct.encode(value.tail)
      }

      override def project[F, G](instance: => Encoder[G], to: F => G, from: G => F): Encoder[F] = new Encoder[F] {
        override def ddl(name: String, mapper: CustomMapper, separator: String): String = instance.ddl(name, mapper, separator)
        override def encode(value: F): Seq[Field] = instance.encode(to(value))
      }

      override def coproduct[L, R <: Coproduct](name: String, cl: => Encoder[L], cr: => Encoder[R]): Encoder[:+:[L, R]] = ???

      override def emptyCoproduct: Encoder[CNil] = ???
    }
  }
}
