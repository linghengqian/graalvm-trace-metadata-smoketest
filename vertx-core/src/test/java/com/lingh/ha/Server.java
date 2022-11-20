package com.lingh.ha;

import io.vertx.core.AbstractVerticle;

import java.lang.management.ManagementFactory;

public class Server extends AbstractVerticle {
    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> {
            final String name = ManagementFactory.getRuntimeMXBean().getName();
            req.response().end("Happily served by " + name);
        }).listen(8080);
    }
}
