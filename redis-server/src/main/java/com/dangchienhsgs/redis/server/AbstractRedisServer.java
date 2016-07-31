package com.dangchienhsgs.redis.server;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import redis.VXRedisProtocol;
import redis.reply.*;

import java.io.IOException;

public abstract class AbstractRedisServer extends AbstractVerticle {

    @Override
    public void start() {
        NetServerOptions serverOptions = new NetServerOptions()
                .setTcpNoDelay(true)   // avoid 2 command in 1 buffer and delay between them
                .setAcceptBacklog(512) // 512 connections in queue, more connection come will be refused
                .setReuseAddress(true);

        vertx.createNetServer(serverOptions).connectHandler(sock -> {
            // create cache storage
            // obtain buffer in a CacheBufStorage class because it must change on some function
            sock.handler(buffer -> {
                try {
                    VXRedisProtocol protocol = new VXRedisProtocol(buffer.getByteBuf());
                    Reply reply = protocol.receive();

                    // free memory of protocol object , avoid leak memory
                    protocol = null;

                    if (reply instanceof MultiBulkReply) {
                        handleMultiBulkReply(sock, (MultiBulkReply) reply);
                    } else if (reply instanceof BulkReply) {
                        handleBulkReply(sock, (BulkReply) reply);
                    } else if (reply instanceof IntegerReply) {
                        handleIntegerReply(sock, (IntegerReply) reply);
                    } else if (reply instanceof StatusReply) {
                        handleStatusReply(sock, (StatusReply) reply);
                    } else if (reply instanceof ErrorReply) {
                        handleErrorReply(sock, (ErrorReply) reply);
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

    public abstract void handleIntegerReply(NetSocket socket, IntegerReply reply);

    public abstract void handleStatusReply(NetSocket socket, StatusReply reply);

    public abstract void handleErrorReply(NetSocket socket, ErrorReply reply);
}
