# Redis protocol in Vertx

- Server:
 
```java
package com.dangchienhsgs.redis.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import redis.reply.*;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class SimpleRedisServer extends AbstractRedisServer {
    private static int NUMBER_SERVER_INSTANCES = 100;

    @Override
    public String getHost() {
        return "127.0.0.1";
    }

    @Override
    public int getPort() {
        return 1234;
    }

    @Override
    public void handleMultiBulkReply(NetSocket socket, MultiBulkReply reply) {
        // do sth
    }

    @Override
    public void handleBulkReply(NetSocket socket, BulkReply reply) {
        // do sth
    }

    @Override
    public void handleIntegerReply(NetSocket socket, IntegerReply reply) {
        // do sth
    }

    @Override
    public void handleStatusReply(NetSocket socket, StatusReply reply) {
        // do sth
    }

    @Override
    public void handleErrorReply(NetSocket socket, ErrorReply reply) {
        // do sth
    }
}
```

Deploy server with load balancing to use multicore :)

```java
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions().setInstances(NUMBER_SERVER_INSTANCES);
        vertx.deployVerticle(SimpleRedisServer.class.getCanonicalName(), options);
    }
```


- Client: Use any redis client like Jedis to send message to server :)
