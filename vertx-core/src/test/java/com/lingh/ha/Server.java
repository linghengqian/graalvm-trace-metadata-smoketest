package com.lingh.ha;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;

import java.lang.management.ManagementFactory;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Launcher.main(new String[]{"run", Server.class.getName(), "-ha"});
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().requestHandler(req -> {
            final String name = ManagementFactory.getRuntimeMXBean().getName();
            req.response().end("Happily served by " + name);
        }).listen(8080);
    }
}
