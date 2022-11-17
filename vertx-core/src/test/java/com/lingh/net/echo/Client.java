package com.lingh.net.echo;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;


public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {
        vertx.createNetClient().connect(1234, "localhost", res -> {

            if (res.succeeded()) {
                NetSocket socket = res.result();
                socket.handler(buffer -> System.out.println("Net client receiving: " + buffer.toString("UTF-8")));
                for (int i = 0; i < 10; i++) {
                    String str = "hello " + i + "\n";
                    System.out.println("Net client sending: " + str);
                    socket.write(str);
                }
            } else {
                System.out.println("Failed to connect " + res.cause());
            }
        });
    }
}
