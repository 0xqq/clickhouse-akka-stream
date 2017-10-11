package io.findify.clickhousesink.encoder
import io.findify.clickhousesink.field.{Field, NestedTable, Row}

class SeqEncoder[T <: Product](implicit encoder: Encoder[T]) extends Encoder[Seq[T]] {
  override def encode(name: String, value: Seq[T]): Seq[Field] = Seq(NestedTable(name, value.map(r => Row(encoder.encode("empty",r)))))
  override def asString(value: Seq[T]): String = ???
}
