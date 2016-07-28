package com.dangchienhsgs.redis.listener;

import io.vertx.core.net.NetSocket;
import redis.reply.BulkReply;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public interface BulkListener {
    void handle(NetSocket socket, BulkReply reply);

}
