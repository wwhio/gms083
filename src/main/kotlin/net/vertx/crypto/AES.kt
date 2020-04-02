package org.shaofan.ms4x.common.net.crypto

import kotlin.experimental.xor

class AES {

    var numRounds = 0
    var Ke: Array<ByteArray>? = null
    var Kd: Array<ByteArray>? = null
    var Ker: ByteArray? = null
    var ta: ByteArray? = null


    companion object {
        val ROUNDS = 14
        val BLOCK_SIZE = 16
        val KEY_LENGTH = 32
        val S = byteArrayOf(
                99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118,
                -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64,
                -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21,
                4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117,
                9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124,
                83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49,
                -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88,
                81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46,
                -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115,
                96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37,
                -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121,
                -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8,
                -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118,
                112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98,
                -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33,
                -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22)
        val Si = byteArrayOf(
                82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5,
                124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53,
                84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78,
                8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37,
                114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110,
                108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124,
                -112, -40, -85, 0, -116, -68, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6,
                -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107,
                58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115,
                -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110,
                71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27,
                -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12,
                31, -35, -88, 51, -120, 7, -57, 49, -79, 18, 16, 89, 39, -128, -20, 95,
                96, 81, 127, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17,
                -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97,
                23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125)
        val rcon = intArrayOf(
                0,
                1, 2, 4, 8, 16, 32,
                64, -128, 27, 54, 108, -40,
                -85, 77, -102, 47, 94, -68,
                99, -58, -105, 53, 106, -44,
                -77, 125, -6, -17, -59, -111)
        val COL_SIZE = 4
        val NUM_COLS = BLOCK_SIZE / COL_SIZE
        val ROOT = 0x11B
        val row_shift = intArrayOf(0, 1, 2, 3)
        val alog = IntArray(256)
        val log = IntArray(256)

        init {
            var i = 1
            var j: Int
            alog[0] = 1

            while (i < 256) {
                j = alog[i - 1] shl 1 xor alog[i - 1]
                if (j and 0x100 !== 0) {
                    j = j xor ROOT
                }
                alog[i] = j
                i++
            }

            i = 1
            while (i < 255) {
                log[alog[i]] = i
                i++
            }
        }
    }

    fun getRounds(keySize: Int): Int {
        return when (keySize) {
            16 -> 10
            24 -> 12
            else -> 14
        }
    }

    fun mul(a: Int, b: Int): Int {
        return if (a != 0 && b != 0) alog[(log[a and 0xFF] + log[b and 0xFF]) % 255] else 0
    }

    fun encrypt(a: ByteArray?): ByteArray? {
        ta = ByteArray(BLOCK_SIZE)
        var i: Int
        var k: Int
        var row: Int
        var col: Int
        requireNotNull(a) { "Empty data to be processed." }
        require(a.size == BLOCK_SIZE) { "Incorrect input length." }
        Ker = Ke?.get(0)
        i = 0
        while (i < BLOCK_SIZE) {
            a[i] = (a[i] xor Ker!![i])
            i++
        }
        for (r in 1 until numRounds) {
            Ker = Ke?.get(r)
            i = 0
            while (i < BLOCK_SIZE) {
                ta!![i] = S[a[i].toInt() and 0xFF]
                i++
            }
            i = 0
            while (i < BLOCK_SIZE) {
                row = i % COL_SIZE
                k = (i + row_shift[row] * COL_SIZE) % BLOCK_SIZE
                a[i] = ta!![k]
                i++
            }
            col = 0
            while (col < NUM_COLS) {
                i = col * COL_SIZE
                ta!![i] = (mul(2, a[i].toInt()) xor mul(3, a[i + 1].toInt()) xor a[i + 2].toInt() xor a[i + 3].toInt()).toByte()
                ta!![i + 1] = (a[i] xor mul(2, a[i + 1].toInt()).toByte() xor mul(3, a[i + 2].toInt()).toByte() xor a[i + 3])
                ta!![i + 2] = (a[i] xor a[i + 1] xor mul(2, a[i + 2].toInt()).toByte() xor mul(3, a[i + 3].toInt()).toByte())
                ta!![i + 3] = (mul(3, a[i].toInt()) xor a[i + 1].toInt() xor a[i + 2].toInt() xor mul(2, a[i + 3].toInt())).toByte()
                col++
            }
            i = 0
            while (i < BLOCK_SIZE) {
                a[i] = (ta!![i] xor Ker!![i])
                i++
            }
        }
        Ker = Ke?.get(numRounds)
        i = 0
        while (i < BLOCK_SIZE) {
            a[i] = S[a[i].toInt() and 0xFF]
            i++
        }
        i = 0
        while (i < BLOCK_SIZE) {
            row = i % COL_SIZE
            k = (i + row_shift[row] * COL_SIZE) % BLOCK_SIZE
            ta!![i] = a[k]
            i++
        }
        i = 0
        while (i < BLOCK_SIZE) {
            a[i] = (ta!![i] xor Ker!![i])
            i++
        }
        ta = null
        Ker = null
        return a
    }

    fun setKey(key: ByteArray?) {
        val BC = BLOCK_SIZE / 4
        val Klen = key!!.size
        val Nk = Klen / 4
        var i: Int
        var j: Int
        var r: Int
        requireNotNull(key) { "Empty key" }
        require(key.size == 16 || key.size == 24 || key.size == 32) { "Incorrect key length" }
        numRounds = getRounds(Klen)
        val ROUND_KEY_COUNT = (numRounds + 1) * BC
        val w0 = ByteArray(ROUND_KEY_COUNT)
        val w1 = ByteArray(ROUND_KEY_COUNT)
        val w2 = ByteArray(ROUND_KEY_COUNT)
        val w3 = ByteArray(ROUND_KEY_COUNT)
        Ke = Array(numRounds + 1) { ByteArray(BLOCK_SIZE) }
        Kd = Array(numRounds + 1) { ByteArray(BLOCK_SIZE) }
        i = 0
        j = 0
        while (i < Nk) {
            w0[i] = key[j++]
            w1[i] = key[j++]
            w2[i] = key[j++]
            w3[i] = key[j++]
            i++
        }
        var t0: Int
        var t1: Int
        var t2: Int
        var t3: Int
        var old0: Int
        i = Nk
        while (i < ROUND_KEY_COUNT) {
            t0 = w0[i - 1].toInt()
            t1 = w1[i - 1].toInt()
            t2 = w2[i - 1].toInt()
            t3 = w3[i - 1].toInt()
            if (i % Nk == 0) {
                old0 = t0
                t0 = S[t1 and 0xFF].toInt() xor rcon[i / Nk]
                t1 = S[t2 and 0xFF].toInt()
                t2 = S[t3 and 0xFF].toInt()
                t3 = S[old0 and 0xFF].toInt()
            } else if (Nk > 6 && i % Nk == 4) {
                t0 = S[t0 and 0xFF].toInt()
                t1 = S[t1 and 0xFF].toInt()
                t2 = S[t2 and 0xFF].toInt()
                t3 = S[t3 and 0xFF].toInt()
            }
            w0[i] = (w0[i - Nk] xor t0.toByte())
            w1[i] = (w1[i - Nk] xor t1.toByte())
            w2[i] = (w2[i - Nk] xor t2.toByte())
            w3[i] = (w3[i - Nk] xor t3.toByte())
            i++
        }
        r = 0
        i = 0
        while (r < numRounds + 1) {
            j = 0
            while (j < BC) {
                Ke!![r][4 * j] = w0[i]
                Ke!![r][4 * j + 1] = w1[i]
                Ke!![r][4 * j + 2] = w2[i]
                Ke!![r][4 * j + 3] = w3[i]
                Kd!![numRounds - r][4 * j] = w0[i]
                Kd!![numRounds - r][4 * j + 1] = w1[i]
                Kd!![numRounds - r][4 * j + 2] = w2[i]
                Kd!![numRounds - r][4 * j + 3] = w3[i]
                i++
                j++
            }
            r++
        }
    }
}
