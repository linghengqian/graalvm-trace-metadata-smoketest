package com.lingh.verticle.worker;

import com.lingh.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(MainVerticle.class, null);
    }

    @Override
    public void start() {
        System.out.println("[Main] Running in " + Thread.currentThread().getName());
        vertx.deployVerticle(com.lingh.verticle.worker.WorkerVerticle.class.getName(),
                new DeploymentOptions().setWorker(true));
        vertx.eventBus().request(
                "sample.data",
                "hello vert.x",
                r -> System.out.printf("[Main] Receiving reply ' %s' in %s%n", r.result().body(), Thread.currentThread().getName())
        );
    }
}
