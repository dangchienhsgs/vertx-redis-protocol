package com.dangchienhsgs.redis.client;

/**
 * Created by Nguyen Dang Chien on 7/26/16.
 */

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RedisClient {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) throws Exception {
        start();
    }

    public static void start() throws Exception {
        Vertx vertx = Vertx.vertx();
        ExecutorService service = Executors.newFixedThreadPool(100);
        vertx.createNetClient().connect(1234, "localhost", res -> {
            if (res.succeeded()) {
                NetSocket socket = res.result();
                Buffer clientBuffer = Buffer.buffer();

                socket.handler(buffer -> {
                    System.out.println("Net client receiving: " + buffer.toString("UTF-8"));
                });

                // Now send some data
                for (int i = 0; i < 1000000; i++) {
                    service.execute(new MyRunnable(i, socket));
                }
            } else {
                System.out.println("Failed to connect " + res.cause());
            }
        });
    }

    public static class MyRunnable implements Runnable {
        private int i;
        private NetSocket netSocket;

        public MyRunnable(int i, NetSocket netSocket) {
            this.i = i;
            this.netSocket = netSocket;
        }

        @Override
        public void run() {
            String send = new StringBuilder("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n")
                    .toString();
            System.out.println(new StringBuilder("Print ").append(i));
            netSocket.write(send);
        }
    }
}
