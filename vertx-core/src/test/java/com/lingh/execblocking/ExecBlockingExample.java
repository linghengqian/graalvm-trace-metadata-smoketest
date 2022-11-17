package com.lingh.execblocking;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;

public class ExecBlockingExample extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(ExecBlockingExample.class, null);
    }

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
