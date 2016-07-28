package redis.handler;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import redis.CacheBufStorage;
import redis.VXRedisProtocol;
import redis.exception.CommandErrorException;
import redis.listener.BulkListener;
import redis.listener.MultiBulkListener;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class OnBufferHandler implements Handler<Buffer> {
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
            System.out.println("New Buffer: " + buffer.toString().replace("\r", "r").replace("\n", "n"));
            protocol = new VXRedisProtocol(buffer, cacheBufStorage);

        } else {
            // have incomplete command
            Buffer totalBuffer = cacheBufStorage.getBuffer().copy();
            totalBuffer.appendBuffer(buffer);
            cacheBufStorage.setBuffer(null);

            System.out.println("Cached, new Buffer: " + totalBuffer.toString().replace("\r", "r").replace("\n", "n"));
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
