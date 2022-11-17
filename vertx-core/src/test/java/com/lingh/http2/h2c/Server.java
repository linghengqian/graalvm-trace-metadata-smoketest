package com.lingh.http2.h2c;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class Server extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Server.class, null);
    }

    @Override
    public void start() {

        HttpServer server =
                vertx.createHttpServer(new HttpServerOptions());

        server.requestHandler(req -> req.response().putHeader("content-type", "text/html").end("<html><body>" +
                "<h1>Hello from vert.x!</h1>" +
                "<p>version = " + req.version() + "</p>" +
                "</body></html>")).listen(8080);
    }
}
