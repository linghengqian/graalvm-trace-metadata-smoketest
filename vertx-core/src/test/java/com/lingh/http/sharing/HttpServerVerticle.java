package com.lingh.http.sharing;

import io.vertx.core.AbstractVerticle;

public class HttpServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> req.response()
                .putHeader("content-type", "text/html")
                .end("<html><body><h1>Hello from " + this + "</h1></body></html>")).listen(8293);
    }
}

