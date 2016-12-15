# scala-iso-8583

A simple implementation of ISO 8583 messages for Scala.

## Prerequisites

* Scala :)
* SBT

## Usage

First of all, you need to provide a definition of your message. A definition is a seq of tuples (Int, BitOps) where BitOps is a type for parser and reducer functions.

```
val isoDef = ISODef(
        3 -> fixed(6),
        4 -> fixed(12),
        7 -> fixed(10),
        11 -> fixed(6),
        44 -> ll,
        105 -> lll
      )
```

The example above we define bit 3 has a fixed length of 6 bytes, bit 4 has a fixed length of 12 and so on. Bits 44 and 105 has variable length which means your length is prefixed with data where ll is prefix of 2 bytes and lll is prefix of 3 bytes.

To parse a message you should use ISODef.parse passing the definition and the message buffer.

```
val Success(isoMsg) = ISODef.parse(isoDef)(msg)
```

Use ISODef.reduce to reduce an ISOMsg to a message buffer

```
val isoMsg = Map(
        0 -> "0200",
        3 -> "000001",
        4 -> "000000000100",
        7 -> "1012194700",
        44 -> "123456",
        105 -> "ABC"
      )

      val buf = ISODef.reduce(isoDef)(isoMsg)
```
