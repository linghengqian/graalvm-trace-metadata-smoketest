package com.lingh.execblocking;

import io.vertx.core.AbstractVerticle;

public class ExecBlockingExample extends AbstractVerticle {
    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(request -> vertx.<String>executeBlocking(promise -> {
            try {
                Thread.sleep(500);
            } catch (Exception ignore) {
            }
            String result = "armadillos!";
            promise.complete(result);
        }, res -> {
            if (res.succeeded()) {
                request.response().putHeader("content-type", "text/plain").end(res.result());
            } else {
                res.cause().printStackTrace();
            }
        })).listen(8080);

    }
}
