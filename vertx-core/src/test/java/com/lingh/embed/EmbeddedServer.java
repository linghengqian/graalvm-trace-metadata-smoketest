package com.lingh.embed;

import io.vertx.core.Vertx;

public class EmbeddedServer {
    public static void main(String[] args) {
        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    }
}
