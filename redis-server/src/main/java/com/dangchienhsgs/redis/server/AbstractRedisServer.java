package com.dangchienhsgs.redis.server;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;
import redis.CacheBufStorage;
import redis.handler.OnBufferHandler;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;

public abstract class AbstractRedisServer extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createNetServer().connectHandler(sock -> {
            // create cache storage
            // obtain buffer in a CacheBufStorage class because it must change on some function
            CacheBufStorage cacheBufStorage = new CacheBufStorage(null);

            // create buffer handler
            OnBufferHandler bufferHandler = new OnBufferHandler(
                    cacheBufStorage,
                    sock,
                    (socket, reply) -> handleMultiBulkReply(socket, reply),
                    (socket, reply) -> handleBulkReply(socket, reply)

            );

            // start listen to buffer
            sock.handler(bufferHandler);

        }).listen(getPort(), getHost());
    }

    public abstract String getHost();

    public abstract int getPort();

    public abstract void handleMultiBulkReply(NetSocket socket, MultiBulkReply reply);

    public abstract void handleBulkReply(NetSocket socket, BulkReply reply);
}
