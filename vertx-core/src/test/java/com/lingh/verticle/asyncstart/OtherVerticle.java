package com.lingh.verticle.asyncstart;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class OtherVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("In OtherVerticle.start (async)");
        vertx.setTimer(2000, tid -> {
            System.out.println("Startup tasks are now complete, OtherVerticle is now started!");
            startPromise.complete();
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        vertx.setTimer(2000, tid -> {
            System.out.println("Cleanup tasks are now complete, OtherVerticle is now stopped!");
            stopPromise.complete();
        });
    }
}
