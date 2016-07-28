package redis.listener;

import io.vertx.core.net.NetSocket;
import redis.reply.MultiBulkReply;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public interface MultiBulkListener {
    void handle(NetSocket socket, MultiBulkReply reply);
}
