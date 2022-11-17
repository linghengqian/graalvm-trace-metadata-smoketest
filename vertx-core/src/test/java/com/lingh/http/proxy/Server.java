package com.lingh.http.proxy;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {

        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("Got request " + req.uri());
            for (String name : req.headers().names()) {
                System.out.println(name + ": " + req.headers().get(name));
            }
            req.handler(data -> System.out.println("Got data " + data.toString("ISO-8859-1")));
            req.endHandler(v -> {
                req.response().setChunked(true);
                for (int i = 0; i < 10; i++) {
                    req.response().write("server-data-chunk-" + i);
                }
                req.response().end();
            });
        }).listen(8282);

    }

}