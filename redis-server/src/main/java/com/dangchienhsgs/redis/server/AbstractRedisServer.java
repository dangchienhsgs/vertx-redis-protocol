package com.dangchienhsgs.redis.server;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import redis.VXRedisProtocol;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

import java.io.IOException;

public abstract class AbstractRedisServer extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createNetServer(new NetServerOptions().setTcpNoDelay(true).setReuseAddress(true))
                .connectHandler(sock -> {
                    // create cache storage
                    // obtain buffer in a CacheBufStorage class because it must change on some function
                    sock.handler(buffer -> {
                        try {
                            VXRedisProtocol protocol = new VXRedisProtocol(buffer.getByteBuf());
                            Reply reply = protocol.receive();

                            // free memory
                            protocol = null;

                            if (reply instanceof MultiBulkReply) {
                                handleMultiBulkReply(sock, (MultiBulkReply) reply);
                            } else if (reply instanceof BulkReply) {
                                handleBulkReply(sock, (BulkReply) reply);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }).listen(getPort(), getHost());
    }

    public abstract String getHost();

    public abstract int getPort();

    public abstract void handleMultiBulkReply(NetSocket socket, MultiBulkReply reply);

    public abstract void handleBulkReply(NetSocket socket, BulkReply reply);
}
