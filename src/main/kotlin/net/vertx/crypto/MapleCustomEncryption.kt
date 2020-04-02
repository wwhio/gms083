package org.shaofan.ms4x.common.net.crypto

class MapleCustomEncryption {
    companion object {
        private fun rollLeft(`in`: Byte, count: Int): Byte {
            var tmp = `in`.toInt() and 0xFF
            tmp = tmp shl count % 8
            return (tmp and 0xFF or (tmp shr 8)).toByte()
        }

        private fun rollRight(`in`: Byte, count: Int): Byte {
            var tmp = `in`.toInt() and 0xFF
            tmp = tmp shl 8 ushr count % 8
            return (tmp and 0xFF or (tmp ushr 8)).toByte()
        }

        fun encryptData(data: ByteArray): ByteArray? {
            for (j in 0..5) {
                var remember: Byte = 0
                var dataLength = (data.size and 0xFF).toByte()
                if (j % 2 == 0) {
                    for (i in data.indices) {
                        var cur = data[i]
                        cur = rollLeft(cur, 3)
                        cur = (cur + dataLength).toByte()
                        cur = (cur.toInt() xor remember.toInt()).toByte()
                        remember = cur
                        cur = rollRight(cur, dataLength.toInt() and 0xFF)
                        cur = (cur.toInt().inv() and 0xFF).toByte()
                        cur = (cur.toInt() + 0x48).toByte()
                        dataLength--
                        data[i] = cur
                    }
                } else {
                    for (i in data.indices.reversed()) {
                        var cur = data[i]
                        cur = rollLeft(cur, 4)
                        cur = (cur + dataLength).toByte()
                        cur = (cur.toInt() xor remember.toInt()).toByte()
                        remember = cur
                        cur = (cur.toInt() xor 0x13).toByte()
                        cur = rollRight(cur, 3)
                        dataLength--
                        data[i] = cur
                    }
                }
            }
            return data
        }

        fun decryptData(data: ByteArray): ByteArray? {
            for (j in 1..6) {
                var remember: Byte = 0
                var dataLength = (data.size and 0xFF).toByte()
                var nextRemember: Byte
                if (j % 2 == 0) {
                    for (i in data.indices) {
                        var cur = data[i]
                        cur = (cur.toInt() - 0x48).toByte()
                        cur = (cur.toInt().inv() and 0xFF).toByte()
                        cur = rollLeft(cur, dataLength.toInt() and 0xFF)
                        nextRemember = cur
                        cur = (cur.toInt() xor remember.toInt()).toByte()
                        remember = nextRemember
                        cur = (cur - dataLength).toByte()
                        cur = rollRight(cur, 3)
                        data[i] = cur
                        dataLength--
                    }
                } else {
                    for (i in data.indices.reversed()) {
                        var cur = data[i]
                        cur = rollLeft(cur, 3)
                        cur = (cur.toInt() xor 0x13).toByte()
                        nextRemember = cur
                        cur = (cur.toInt() xor remember.toInt()).toByte()
                        remember = nextRemember
                        cur = (cur - dataLength).toByte()
                        cur = rollRight(cur, 4)
                        data[i] = cur
                        dataLength--
                    }
                }
            }
            return data
        }
    }
}
