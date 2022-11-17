package com.lingh.http.simple;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;


public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> req.response().putHeader("content-type", "text/html").end("<html><body><h1>Hello from vert.x!</h1></body></html>")).listen(8080);
    }
}
