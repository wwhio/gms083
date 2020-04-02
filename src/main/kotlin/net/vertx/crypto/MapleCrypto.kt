package org.shaofan.ms4x.common.net.crypto

class MapleCrypto {

    companion object {
        /**
         * Used for checking the packets being sent and received. These are
         * initialized at the starting part of the program by providing the
         * current version the server is running.
         *
         * @see MapleAES.checkPacket
         * @see MapleAES.checkPacket
         * @see MapleAES.initialize
         */
        private var gVersion: Short = 0

        /**
         * Used for checking the packets being sent and received. These are
         * initialized at the starting part of the program by providing the
         * current version the server is running.
         *
         * @see MapleAES.checkPacket
         * @see MapleAES.checkPacket
         * @see MapleAES.initialize
         */
        private var sVersion: Short = 0

        /**
         * Used for checking the packets being sent and received. These are
         * initialized at the starting part of the program by providing the
         * current version the server is running.
         *
         * @see MapleAES.checkPacket
         * @see MapleAES.checkPacket
         * @see MapleAES.initialize
         */
        private var rVersion: Short = 0

        /**
         * Used for renewing the cryptography seed for sending or receiving
         * packets.
         *
         */
        val SHUFFLE_BYTES = intArrayOf(0xEC, 0x3F, 0x77, 0xA4, 0x45, 0xD0, 0x71, 0xBF, 0xB7, 0x98, 0x20, 0xFC,
                0x4B, 0xE9, 0xB3, 0xE1, 0x5C, 0x22, 0xF7, 0x0C, 0x44, 0x1B, 0x81, 0xBD, 0x63, 0x8D, 0xD4, 0xC3,
                0xF2, 0x10, 0x19, 0xE0, 0xFB, 0xA1, 0x6E, 0x66, 0xEA, 0xAE, 0xD6, 0xCE, 0x06, 0x18, 0x4E, 0xEB,
                0x78, 0x95, 0xDB, 0xBA, 0xB6, 0x42, 0x7A, 0x2A, 0x83, 0x0B, 0x54, 0x67, 0x6D, 0xE8, 0x65, 0xE7,
                0x2F, 0x07, 0xF3, 0xAA, 0x27, 0x7B, 0x85, 0xB0, 0x26, 0xFD, 0x8B, 0xA9, 0xFA, 0xBE, 0xA8, 0xD7,
                0xCB, 0xCC, 0x92, 0xDA, 0xF9, 0x93, 0x60, 0x2D, 0xDD, 0xD2, 0xA2, 0x9B, 0x39, 0x5F, 0x82, 0x21,
                0x4C, 0x69, 0xF8, 0x31, 0x87, 0xEE, 0x8E, 0xAD, 0x8C, 0x6A, 0xBC, 0xB5, 0x6B, 0x59, 0x13, 0xF1,
                0x04, 0x00, 0xF6, 0x5A, 0x35, 0x79, 0x48, 0x8F, 0x15, 0xCD, 0x97, 0x57, 0x12, 0x3E, 0x37, 0xFF,
                0x9D, 0x4F, 0x51, 0xF5, 0xA3, 0x70, 0xBB, 0x14, 0x75, 0xC2, 0xB8, 0x72, 0xC0, 0xED, 0x7D, 0x68,
                0xC9, 0x2E, 0x0D, 0x62, 0x46, 0x17, 0x11, 0x4D, 0x6C, 0xC4, 0x7E, 0x53, 0xC1, 0x25, 0xC7, 0x9A,
                0x1C, 0x88, 0x58, 0x2C, 0x89, 0xDC, 0x02, 0x64, 0x40, 0x01, 0x5D, 0x38, 0xA5, 0xE2, 0xAF, 0x55,
                0xD5, 0xEF, 0x1A, 0x7C, 0xA7, 0x5B, 0xA6, 0x6F, 0x86, 0x9F, 0x73, 0xE6, 0x0A, 0xDE, 0x2B, 0x99,
                0x4A, 0x47, 0x9C, 0xDF, 0x09, 0x76, 0x9E, 0x30, 0x0E, 0xE4, 0xB2, 0x94, 0xA0, 0x3B, 0x34, 0x1D,
                0x28, 0x0F, 0x36, 0xE3, 0x23, 0xB4, 0x03, 0xD8, 0x90, 0xC8, 0x3C, 0xFE, 0x5E, 0x32, 0x24, 0x50,
                0x1F, 0x3A, 0x43, 0x8A, 0x96, 0x41, 0x74, 0xAC, 0x52, 0x33, 0xF0, 0xD9, 0x29, 0x80, 0xB1, 0x16,
                0xD3, 0xAB, 0x91, 0xB9, 0x84, 0x7F, 0x61, 0x1E, 0xCF, 0xC5, 0xD1, 0x56, 0x3D, 0xCA, 0xF4, 0x05,
                0xC6, 0xE5, 0x08, 0x49)

        /**
         * Initializes the send and receive version values for net.swordie.ms.connection.packet
         * checking using the current version.
         *
         * @param v the current version this service is running.
         */
        fun initialize(v: Short) {
            gVersion = v
            sVersion = (0xFFFF - v ushr 8 and 0xFF or (0xFFFF - v shl 8 and 0xFF00)).toShort()
            rVersion = (v.toInt() ushr 8 and 0xFF or (v.toInt() shl 8 and 0xFF00)).toShort()
        }

        /**
         * Creates a header for the new net.swordie.ms.connection.packet to be sent to the opposite
         * end of the session (Server to Channel for this engine). Contains
         * information about the net.swordie.ms.connection.packet: whether or not it is valid and the actual
         * length of the net.swordie.ms.connection.packet.
         *
         * @param delta the input net.swordie.ms.connection.packet length before adding the header.
         * @param gamma the input sending seed before changing it.
         *
         * @return the header to be sent with this net.swordie.ms.connection.packet.
         */
        fun getHeader(delta: Int, gamma: ByteArray): ByteArray? {
            var a: Int = gamma[3].toInt() and 0xFF
            a = a or (gamma[2].toInt() shl 8 and 0xFF00)
            a = a xor sVersion.toInt()
            val b = delta shl 8 and 0xFF00 or (delta ushr 8)
            val c = a xor b
            val ret = ByteArray(4)
            ret[0] = (a ushr 8 and 0xFF).toByte()
            ret[1] = (a and 0xFF).toByte()
            ret[2] = (c ushr 8 and 0xFF).toByte()
            ret[3] = (c and 0xFF).toByte()
            return ret
        }

        fun getHeader(delta: Int, gamma: ByteArray, v: Short): ByteArray? {
            var a: Int = gamma[3].toInt() and 0xFF
            a = a or (gamma[2].toInt() shl 8 and 0xFF00)
            a = a xor sVersion.toInt()
            val b = delta shl 8 and 0xFF00 or (delta ushr 8)
            val c = a xor b
            val ret = ByteArray(4)
            ret[0] = (a ushr 8 and 0xFF).toByte()
            ret[1] = (a and 0xFF).toByte()
            ret[2] = (c ushr 8 and 0xFF).toByte()
            ret[3] = (c and 0xFF).toByte()
            return ret
        }

        /**
         * Gets the length of the net.swordie.ms.connection.packet given the received header.
         *
         * @param packetHeader the net.swordie.ms.connection.packet header to be used to find the net.swordie.ms.connection.packet length.
         *
         * @return the length of the received net.swordie.ms.connection.packet.
         */
        fun getLength(packetHeader: ByteArray): Int {
            //read two 16-bit little-endian integers and XOR them.
            return packetHeader[0].toInt() and 0xFF or (packetHeader[1].toInt() and 0xFF shl 8) xor
                    (packetHeader[2].toInt() and 0xFF or (packetHeader[3].toInt() and 0xFF shl 8))
        }

        /**
         * Checks the net.swordie.ms.connection.packet to make sure it is valid and that the session
         * between the net.swordie.ms.client and server is secure and legitimate.
         *
         * @param delta the net.swordie.ms.connection.packet header from the received net.swordie.ms.connection.packet (4 bytes in length).
         * @param gamma the current receive seed.
         *
         * @return whether or not the net.swordie.ms.connection.packet is valid (consequently, if not valid,
         * the session is terminated usually).
         */
        fun checkPacket(delta: ByteArray, gamma: ByteArray): Boolean {
            return (delta[0].toInt() xor gamma[2].toInt() and 0xFF == rVersion.toInt() ushr 8 and 0xFF
                    && delta[1].toInt() xor gamma[3].toInt() and 0xFF == rVersion.toInt() and 0xFF)
        }

        /**
         * @see MapleAES.checkPacket
         * @param delta foreign header.
         * @param gamma current receive seed.
         */
        fun checkPacket(delta: Int, gamma: ByteArray): Boolean {
            val a = ByteArray(2)
            a[0] = (delta ushr 24 and 0xFF).toByte()
            a[1] = (delta ushr 16 and 0xFF).toByte()
            return checkPacket(a, gamma)
        }

        /**
         * Shuffles the seed (or IV) used for this cryptography stage.
         * Almost always called when this cryptography stage is finished since
         * rolling the seed is important to keeping a valid session.
         *
         * @param delta the old seed or IV to be changed into the new one.
         *
         * @return the new seed or IV to be used for this stage of cryptography
         * for the next net.swordie.ms.connection.packet sent or received.
         */
        fun getNewIv(delta: ByteArray): ByteArray {
            val nIv = intArrayOf(0xF2, 0x53, 0x50, 0xC6)
            for (i in 0..3) {
                val a: Int = delta[i].toInt() and 0xFF
                val b = SHUFFLE_BYTES[a]
                nIv[0] += SHUFFLE_BYTES[nIv[1]] - a
                nIv[1] -= nIv[2] xor b
                nIv[2] = nIv[2] xor SHUFFLE_BYTES[nIv[3]] + a
                nIv[3] -= nIv[0] - b
                var c = nIv[0] and 0xFF
                c = c or (nIv[1] shl 8 and 0xFF00)
                c = c or (nIv[2] shl 16 and 0xFF0000)
                c = c or (nIv[3] shl 24 and -0x1000000)
                val d = c shl 3 or (c ushr 0x1D)
                nIv[0] = d and 0xFF
                nIv[1] = d ushr 8 and 0xFF
                nIv[2] = d ushr 16 and 0xFF
                nIv[3] = d ushr 24 and 0xFF
            }
            for (i in 0..3) {
                delta[i] = nIv[i].toByte()
            }
            return delta
        }
    }

    /**
     * AES cipher used for cryptography.
     *
     * @see AES.encrypt
     */
    private val cipher: AES = AES()

    init {
        // key changes later on in version
        cipher.setKey(byteArrayOf(
                0x13, 0x00, 0x00, 0x00,
                0x08, 0x00, 0x00, 0x00,
                0x06, 0x00, 0x00, 0x00,
                0xB4.toByte(), 0x00, 0x00, 0x00,
                0x1B, 0x00, 0x00, 0x00,
                0x0F, 0x00, 0x00, 0x00,
                0x33, 0x00, 0x00, 0x00,
                0x52, 0x00, 0x00, 0x00
        ))
    }

    /**
     * Cryptography segment of MapleAESOFB.
     *
     * @param delta the input data to be put into stage for finalized encryption
     * or to be finally decryption.
     * @param gamma the input seed for this cryptography stage. This value is
     * renewed after each encryption by the corresponding encoder or decoder.
     *
     * @return the bytes having been converted to a stage for encryption or
     * being fully decrypted.
     */
    fun crypt(delta: ByteArray, gamma: ByteArray): ByteArray {
        var a = delta.size
        var b = 0x5B0
        var c = 0
        while (a > 0) {
            val d: ByteArray = BitTools.multiplyBytes(gamma, 4, 4)
            if (a < b) {
                b = a
            }
            for (e in c until c + b) {
                if ((e - c) % d.size == 0) {
                    try {
                        cipher.encrypt(d)
                    } catch (ex: Exception) {
                        ex.printStackTrace() // may eventually want to remove this
                    }
                }
                delta[e] = (delta[e].toInt() xor d[(e - c) % d.size].toInt()).toByte()
            }
            c += b
            a -= b
            b = 0x5B4
        }
        return delta
    }
}
