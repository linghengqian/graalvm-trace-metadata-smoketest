package com.lingh.net.echossl;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;


public class Client extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(Client.class);
    }

    @Override
    public void start() {
        NetClientOptions options = new NetClientOptions().setSsl(true).setTrustAll(true);
        vertx.createNetClient(options).connect(1234, "localhost", res -> {
            if (res.succeeded()) {
                NetSocket sock = res.result();
                sock.handler(buff -> System.out.println("client receiving " + buff.toString("UTF-8")));
                for (int i = 0; i < 10; i++) {
                    String str = "hello " + i + "\n";
                    System.out.println("Net client sending: " + str);
                    sock.write(str);
                }
            } else {
                System.out.println("Failed to connect " + res.cause());
            }
        });
    }
}
