package org.shaofan.ms4x.common.net.crypto

class BitTools {
    companion object {
        fun multiplyBytes(iv: ByteArray, i: Int, i0: Int): ByteArray {
            val ret = ByteArray(i * i0)
            for (x in ret.indices) {
                ret[x] = iv[x % i]
            }
            return ret
        }

        fun rollLeft(`in`: Byte, count: Int): Byte {
            return (`in`.toInt() and 0xFF shl count % 8 and 0xFF
                    or (`in`.toInt() and 0xFF shl count % 8 shr 8)).toByte()
        }

        fun rollRight(`in`: Byte, count: Int): Byte {
            var tmp = `in`.toInt() and 0xFF
            tmp = tmp shl 8 ushr count % 8
            return (tmp and 0xFF or (tmp ushr 8)).toByte()
        }
    }
}
