package io.findify.clickhouse.format

import akka.util.ByteString
import io.findify.clickhouse.format.Field.{CString, Int64, Row, UInt64}
import io.findify.clickhouse.format.input.JSONInputFormat
import org.scalatest.{FlatSpec, Matchers}
import sun.nio.cs.StandardCharsets

class JSONInputFormatTest extends FlatSpec with Matchers {
  val dec = new JSONInputFormat()
  it should "decode sample messages" in {
    val example = """{
                    |        "meta":
                    |        [
                    |                {
                    |                        "name": "SearchPhrase",
                    |                        "type": "String"
                    |                },
                    |                {
                    |                        "name": "c",
                    |                        "type": "UInt64"
                    |                }
                    |        ],
                    |
                    |        "data":
                    |        [
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "8267016"
                    |                },
                    |                {
                    |                        "SearchPhrase": "интерьер ванной комнаты",
                    |                        "c": "2166"
                    |                },
                    |                {
                    |                        "SearchPhrase": "яндекс",
                    |                        "c": "1655"
                    |                },
                    |                {
                    |                        "SearchPhrase": "весна 2014 мода",
                    |                        "c": "1549"
                    |                },
                    |                {
                    |                        "SearchPhrase": "фриформ фото",
                    |                        "c": "1480"
                    |                }
                    |        ],
                    |
                    |        "totals":
                    |        {
                    |                "SearchPhrase": "",
                    |                "c": "8873898"
                    |        },
                    |
                    |        "extremes":
                    |        {
                    |                "min":
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "1480"
                    |                },
                    |                "max":
                    |                {
                    |                        "SearchPhrase": "",
                    |                        "c": "8267016"
                    |                }
                    |        },
                    |
                    |        "rows": 5,
                    |
                    |        "rows_before_limit_at_least": 141137
                    |}
                    |""".stripMargin
    val response = dec.read(ByteString(example))
    response.right.get.data.size shouldBe 5
  }
  it should "deal with arrays" in {
    val input =
      """
        |{
        |        "meta":
        |        [
        |                {
        |                        "name": "key",
        |                        "type": "String"
        |                },
        |                {
        |                        "name": "strvalues",
        |                        "type": "Array(String)"
        |                },
        |                {
        |                        "name": "intvalues",
        |                        "type": "Array(UInt8)"
        |                },
        |                {
        |                        "name": "floatvalues",
        |                        "type": "Array(Float32)"
        |                },
        |                {
        |                        "name": "longvalues",
        |                        "type": "Array(UInt64)"
        |                }
        |        ],
        |
        |        "data":
        |        [
        |                {
        |                        "key": "foo",
        |                        "strvalues": ["bar","baz"],
        |                        "intvalues": [1,2,3],
        |                        "floatvalues": [0.1,0.2,0.3],
        |                        "longvalues": ["1","2","3"]
        |                }
        |        ],
        |
        |        "rows": 1,
        |
        |        "statistics":
        |        {
        |                "elapsed": 0.000462454,
        |                "rows_read": 1,
        |                "bytes_read": 107
        |        }
        |}
      """.stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data.size shouldBe 1
  }

  it should "work with nulls" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "key",
                  |                        "type": "String"
                  |                },
                  |                {
                  |                        "name": "v",
                  |                        "type": "Nullable(String)"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                {
                  |                        "key": "foo",
                  |                        "v": null
                  |                }
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000334462,
                  |                "rows_read": 1,
                  |                "bytes_read": 22
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data.size shouldBe 1
  }
  it should "work with 64-bit ints" in {
    val input = """{
                  |        "meta":
                  |        [
                  |                {
                  |                        "name": "key",
                  |                        "type": "String"
                  |                },
                  |                {
                  |                        "name": "a",
                  |                        "type": "Int64"
                  |                },
                  |                {
                  |                        "name": "b",
                  |                        "type": "UInt64"
                  |                }
                  |        ],
                  |
                  |        "data":
                  |        [
                  |                {
                  |                        "key": "foo",
                  |                        "a": "123",
                  |                        "b": "456"
                  |                }
                  |        ],
                  |
                  |        "rows": 1,
                  |
                  |        "statistics":
                  |        {
                  |                "elapsed": 0.000334462,
                  |                "rows_read": 1,
                  |                "bytes_read": 22
                  |        }
                  |}""".stripMargin
    val response = dec.read(ByteString(input))
    response.right.get.data shouldBe List(Row(Map("key" -> CString("foo"), "a" -> Int64(123), "b" -> UInt64(456))))
  }
}
