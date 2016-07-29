package com.dangchienhsgs.redis.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import redis.reply.BulkReply;
import redis.reply.MultiBulkReply;
import redis.reply.Reply;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class SimpleRedisServer extends AbstractRedisServer {
    private static int NUMBER_SERVER_INSTANCES = 30;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions().setInstances(NUMBER_SERVER_INSTANCES);
        vertx.deployVerticle(SimpleRedisServer.class.getCanonicalName(), options);
    }

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
        String result = "";
        for (int i = 0; i < reply.data().length; i++) {
            Reply rep = reply.data()[i];

            if (rep instanceof BulkReply) {
                result = result + " " + ((BulkReply) rep).asUTF8String();
            }
        }

        System.out.println("Array: " + result);
        socket.write("*1\r\n$2\r\nok\r\n");
    }

    @Override
    public void handleBulkReply(NetSocket socket, BulkReply reply) {
        System.out.println("String: " + reply.asUTF8String());
    }
}
