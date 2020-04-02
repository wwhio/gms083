package net.vertx

import io.vertx.core.AbstractVerticle
import net.PacketProcessor

class ChannelVerticle : AbstractVerticle() {
    override fun start() {
        val config = config()

        vertx.createNetServer().connectHandler { socket ->
            ClientSession(socket, vertx, PacketProcessor.getProcessor(config.getInteger("world"), config.getInteger("channel")))
        }.listen(config.getInteger("port")) { res ->
            if (res.succeeded()) {
                println("启动成功")
            } else {
                println("启动失败")
            }
        }
    }
}