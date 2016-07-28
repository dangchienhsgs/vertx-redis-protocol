package redis.listener;

import redis.reply.Reply;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public interface ReplyListener {
    void handle(Reply reply);
}
