package com.lingh.net.echo;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.streams.Pump;


public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() throws Exception {
        vertx.createNetServer().connectHandler(sock -> {
            Pump.pump(sock, sock).start();
        }).listen(1234);
        System.out.println("Echo server is now listening");

    }
}
