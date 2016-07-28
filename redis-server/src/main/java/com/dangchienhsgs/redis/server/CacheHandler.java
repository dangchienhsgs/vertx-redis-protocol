package com.dangchienhsgs.redis.server;

import io.vertx.core.buffer.Buffer;

/**
 * Created by Nguyen Dang Chien on 7/27/16.
 */
public class CacheHandler {
    private Buffer buffer;

    public CacheHandler(Buffer buffer) {
        this.buffer = buffer;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }
}
