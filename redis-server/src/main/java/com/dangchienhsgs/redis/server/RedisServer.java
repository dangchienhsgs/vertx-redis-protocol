package com.dangchienhsgs.redis.server;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import com.dangchienhsgs.redis.exception.CommandErrorException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RedisServer extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions().setInstances(100);
        vertx.deployVerticle(RedisServer.class.getCanonicalName(), options);
    }

    @Override
    public void start() {
        vertx.createNetServer().connectHandler(sock -> {
            // create cache handler
            CacheHandler cacheHandler = new CacheHandler(null);
            sock.handler(new BufferHandler(cacheHandler));
        }).listen(1234);
        System.out.println("Echo server is now listening");
    }


    private static class BufferHandler implements Handler<Buffer> {
        private CacheHandler cacheHandler;

        public BufferHandler(CacheHandler handler) {
            this.cacheHandler = handler;
        }

        @Override
        public void handle(Buffer buffer) {
            VXRedisProtocol protocol;
            if (cacheHandler.getBuffer() == null) {
                //System.out.println("New Buffer: " + buffer.toString().replace("\r", "r").replace("\n", "n"));
                protocol = new VXRedisProtocol(buffer, cacheHandler);
            } else {
                Buffer totalBuffer = cacheHandler.getBuffer().copy();
                totalBuffer.appendBuffer(buffer);
                cacheHandler.setBuffer(null);
                //System.out.println("Cached, new Buffer: " + totalBuffer.toString().replace("\r", "r").replace("\n", "n"));
                protocol = new VXRedisProtocol(totalBuffer, cacheHandler);
            }
            while (protocol.getNumberUnreadByte() > 0) {
                try {
                    Reply reply = protocol.receive(false);

                    if (reply == null) {
                        // read all but have a incomplete command
                        // cache the incomplete part
                        break;
                    }

                    // read finish
                    // cacheHandler.setBuffer(null);
                    if (reply instanceof BulkReply) {
                        String message = ((BulkReply) reply).asAsciiString();
                        //System.out.println("Receive " + message);
                    } else if (reply instanceof MultiBulkReply) {
                        MultiBulkReply multiBulkReply = (MultiBulkReply) reply;
                        for (int i = 0; i < multiBulkReply.data().length; i++) {
                            //System.out.println("Receive: " + ((BulkReply) multiBulkReply.data()[i]).asAsciiString());
                        }
                    }
                } catch (CommandErrorException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }


}
