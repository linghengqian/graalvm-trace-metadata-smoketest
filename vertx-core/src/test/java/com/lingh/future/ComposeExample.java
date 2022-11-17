package com.lingh.future;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.Function;

public class ComposeExample extends AbstractVerticle {
    public static void main(String[] args) {
        Runner.runExample(ComposeExample.class);
    }

    @Override
    public void start() throws Exception {
        Future<String> future = anAsyncAction();
        future.compose(this::anotherAsyncAction)
                .onComplete(ar -> {
                    if (ar.failed()) {
                        System.out.println("Something bad happened");
                        ar.cause().printStackTrace();
                    } else {
                        System.out.println("Result: " + ar.result());
                    }
                });
    }

    private Future<String> anAsyncAction() {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(100, l -> promise.complete("world"));
        return promise.future();
    }

    private Future<String> anotherAsyncAction(String name) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(100, l -> promise.complete("hello " + name));
        return promise.future();
    }


}
