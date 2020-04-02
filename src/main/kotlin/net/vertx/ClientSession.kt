package net.vertx

import client.MapleClient
import cn.hutool.core.util.RandomUtil
import constants.net.ServerConstants
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import net.MaplePacketHandler
import net.PacketProcessor
import org.shaofan.ms4x.common.net.crypto.MapleCrypto
import org.shaofan.ms4x.common.net.crypto.MapleCustomEncryption
import tools.MapleAESOFB
import tools.MapleLogger
import tools.MaplePacketCreator
import tools.data.input.ByteArrayByteStream
import tools.data.input.GenericSeekableLittleEndianAccessor
import tools.data.input.SeekableLittleEndianAccessor
import kotlin.math.min

class ClientSession(private val socket: NetSocket, val vertx: Vertx, val pp: PacketProcessor) {


    companion object {
        val EMPTY_BUFFER: Buffer = Buffer.buffer()
        const val HEADER_LENGTH: Int = 4
    }

    enum class MessageType {
        HEADER, BODY
    }

    private var rxIv: ByteArray
    private var txIv: ByteArray
    private var lastRxBuf: Buffer
    private var rxType: MessageType
    private var rxLen: Int
    private val mCr: MapleCrypto = MapleCrypto()
    var client: MapleClient
    var HWID2: String? = null
    var HWID: String? = null
    var TRANSITION: Boolean = false

    init {
        socket.handler(this::receive)
        rxLen = HEADER_LENGTH
        rxType = MessageType.HEADER
        lastRxBuf = EMPTY_BUFFER

        rxIv = RandomUtil.randomBytes(4)
        txIv = RandomUtil.randomBytes(4)

        val sendCypher = MapleAESOFB(txIv, (0xFFFF - ServerConstants.VERSION).toShort())
        val recvCypher = MapleAESOFB(rxIv, ServerConstants.VERSION)
        val client = MapleClient(sendCypher, recvCypher, this)
        this.client = client
        socket.write(Buffer.buffer(MaplePacketCreator.getHello(ServerConstants.VERSION, txIv, rxIv)))

    }

    private fun receive(buf: Buffer) {
        val lastBufLength = lastRxBuf.length()

        //keep processing packet header/body as long as we have enough bytes to read it in full
        var cursor = 0
        while (cursor - lastBufLength + rxLen <= buf.length()) {
            var message: ByteArray
            if (cursor - lastBufLength >= 0) {
                //lastBuf is exhausted
                message = buf.getBytes(cursor - lastBufLength, cursor - lastBufLength + rxLen)
                cursor += rxLen
            } else {
                //first finishes off lastBuf if it's not empty, then goes to buf
                val readFromLastBuf = min(lastBufLength - cursor, rxLen)
                message = ByteArray(rxLen)
                System.arraycopy(lastRxBuf.getBytes(cursor, readFromLastBuf), 0, message, 0, readFromLastBuf)
                cursor += rxLen
                rxLen -= readFromLastBuf
                if (rxLen > 0) System.arraycopy(buf.getBytes(0, rxLen), 0, message, readFromLastBuf, rxLen)
            }
            when (rxType) {
                MessageType.HEADER -> {
                    if (!MapleCrypto.checkPacket(message, rxIv)) {
                        return
                    }
                    rxLen = MapleCrypto.getLength(message)
                    rxType = MessageType.BODY
                }
                MessageType.BODY -> {
                    rxLen = HEADER_LENGTH
                    rxType = MessageType.HEADER
                    decryptBytes(message)
                    val slea: SeekableLittleEndianAccessor = GenericSeekableLittleEndianAccessor(ByteArrayByteStream(message))
                    val packetId = slea.readShort()

                    val packetHandler: MaplePacketHandler = pp.getHandler(packetId)
                    if (packetHandler != null && packetHandler.validateState(client)) {
                        try {
                            MapleLogger.logRecv(client, packetId, message)
                            packetHandler.handlePacket(slea, client)
                        } catch (t: Throwable) {

                            //client.announce(MaplePacketCreator.enableActions());//bugs sometimes
                        }
                        client.updateLastPacket()
                    }
                }
            }
        }

        //check if buffer contains more than we can read. if so, keep a copy of the remaining bytes
        lastRxBuf = if (cursor - lastBufLength < buf.length()) buf.getBuffer(cursor - lastBufLength, buf.length()) else EMPTY_BUFFER
    }

    private fun decryptBytes(body: ByteArray) {
        mCr.crypt(body, rxIv)
        MapleCustomEncryption.decryptData(body)
        rxIv = MapleCrypto.getNewIv(rxIv)
    }

    private fun encryptedBytes(message: ByteArray): Buffer {

        val header = MapleCrypto.getHeader(message.size, txIv)
        MapleCustomEncryption.encryptData(message)
        mCr.crypt(message, txIv)
        txIv = MapleCrypto.getNewIv(txIv)
        val buffer = Buffer.buffer()
        buffer.appendBytes(header)
        buffer.appendBytes(message)
        return buffer
    }

    fun write(data: ByteArray) {
        socket.write(encryptedBytes(data))
    }

    fun getRemoteAddress(): String {
        return socket.remoteAddress().toString()
    }

    fun close() {
        socket.close()
    }

}
