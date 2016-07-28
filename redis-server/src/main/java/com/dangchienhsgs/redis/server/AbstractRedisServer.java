package com.dangchienhsgs.redis.server;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import com.dangchienhsgs.redis.exception.CommandErrorException;
import com.dangchienhsgs.redis.listener.BulkListener;
import com.dangchienhsgs.redis.listener.MultiBulkListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

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

    private static class OnBufferHandler implements Handler<Buffer> {
        private CacheBufStorage cacheBufStorage;
        private NetSocket socket;
        private MultiBulkListener multiBulkListener;
        private BulkListener bulkListener;

        public OnBufferHandler(CacheBufStorage cacheBufStorage, NetSocket socket, MultiBulkListener multiBulkListener, BulkListener bulkListener) {
            this.cacheBufStorage = cacheBufStorage;
            this.socket = socket;
            this.multiBulkListener = multiBulkListener;
            this.bulkListener = bulkListener;
        }

        @Override
        public void handle(Buffer buffer) {
            VXRedisProtocol protocol;
            if (cacheBufStorage.getBuffer() == null) {
                // do not have some incomplete command
                // System.out.println("New Buffer: " + buffer.toString().replace("\r", "r").replace("\n", "n"));
                protocol = new VXRedisProtocol(buffer, cacheBufStorage);

            } else {
                // have incomplete command
                Buffer totalBuffer = cacheBufStorage.getBuffer().copy();
                totalBuffer.appendBuffer(buffer);
                cacheBufStorage.setBuffer(null);

                // System.out.println("Cached, new Buffer: " + totalBuffer.toString().replace("\r", "r").replace("\n", "n"));
                protocol = new VXRedisProtocol(totalBuffer, cacheBufStorage);
            }

            while (protocol.getNumberUnreadByte() > 0) {
                try {
                    Reply reply = protocol.receive(false);

                    if (reply == null) {
                        // read all but have a incomplete command
                        // cache the incomplete part
                        break;
                    }

                    if (reply instanceof BulkReply) {
                        bulkListener.handle(socket, (BulkReply) reply);
                    } else if (reply instanceof MultiBulkReply) {
                        multiBulkListener.handle(socket, (MultiBulkReply) reply);
                    }

                } catch (CommandErrorException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
